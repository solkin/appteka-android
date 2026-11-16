package com.tomclaw.appsend.install

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.tomclaw.appsend.download.DownloadNotifications
import com.tomclaw.appsend.util.ActivityStarter
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.getParcelableExtraCompat
import com.tomclaw.appsend.util.logDebug
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Installs through a [PackageInstaller] session, which makes Appteka the installer
 * of record. That is what lets later updates skip the system's confirmation where
 * Android allows it; every other case comes back as a confirmation to show.
 *
 * The system gives no word when the user accepts its question, only the final result.
 * So a question is shown exactly once, and from then on the install is the system's.
 */
class SessionInstallMethod(
    private val context: Context,
    tracker: InstallTracker,
    private val notifications: DownloadNotifications,
    private val analytics: Analytics,
    private val activities: ActivityStarter,
) : InstallMethod {

    private val packageInstaller = context.packageManager.packageInstaller
    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val sessions = InstallSessions(tracker)
    private val committed = CommittedSessions(context)

    // Questions that came while the user was in none of our screens, until they are shown
    private val confirmations = ConcurrentHashMap<InstallSessions.Attempt, Intent>()

    init {
        restoreSessions()
    }

    override fun install(request: InstallRequest) {
        // One that is already on its way has nothing to start over
        val attempt = sessions.start(request.key) ?: return
        sessions.withCurrent(attempt) {
            notifications.cancelInstall(request.key)
            startService(request)
        }
        executor.execute { writeAndCommit(request, attempt) }
    }

    override fun confirm(key: String, confirmation: Intent?) {
        val attempt = sessions.current(key) ?: return
        sessions.withCurrent(attempt) {
            val question = confirmations[attempt] ?: confirmation ?: return@withCurrent
            if (attempt.id?.let { packageInstaller.getSessionInfo(it) } == null) {
                // Gone without a result, so there is nothing left to answer
                notifications.cancelInstall(key)
                finish(attempt)
            } else if (activities.start(question)) {
                confirmations.remove(attempt)
                notifications.cancelInstall(key)
                sessions.update(attempt, InstallStatus.Installing)
            }
        }
    }

    private fun startService(request: InstallRequest) {
        try {
            ContextCompat.startForegroundService(
                context,
                createInstallServiceIntent(context, request)
            )
        } catch (ex: IllegalStateException) {
            // Refused while the app is in the background, as when a download completes there.
            // The delivery's own service keeps the process alive through the copy then
            logDebug("[install] Install service not started: ${ex.message}")
        }
    }

    override fun cancel(key: String) {
        val attempt = sessions.current(key) ?: return
        val cancelled = sessions.withCurrent(attempt) {
            notifications.cancelInstall(key)
            finish(attempt)
        }
        if (cancelled) {
            attempt.id?.let { abandon(it) }
        }
    }

    fun onResult(intent: Intent) {
        val key = intent.getStringExtra(EXTRA_KEY) ?: return
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()
        val uri = intent.getParcelableExtraCompat(EXTRA_URI, Uri::class.java)
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val sessionId = intent.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, NO_SESSION)
        sessions.onResult(key, sessionId) { attempt ->
            when (status) {
                PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                    val confirmation = intent.getParcelableExtraCompat(Intent.EXTRA_INTENT, Intent::class.java)
                    if (confirmation == null) {
                        fail(attempt, label, uri, InstallFailure.UNKNOWN, packageToRemove = null)
                    } else {
                        requestConfirmation(attempt, label, confirmation)
                    }
                }

                PackageInstaller.STATUS_SUCCESS -> {
                    notifications.cancelInstall(key)
                    finish(attempt)
                }

                else -> onFailure(intent, attempt, label, uri, status)
            }
        }
    }

    private fun onFailure(
        intent: Intent,
        attempt: InstallSessions.Attempt,
        label: String,
        uri: Uri?,
        status: Int,
    ) {
        val statusMessage = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val failure = InstallFailure.from(status, statusMessage)
        if (failure == null) {
            notifications.cancelInstall(attempt.key)
            finish(attempt)
            return
        }
        // The other package is the one to blame when the system names one
        val packageToRemove = intent.getStringExtra(PackageInstaller.EXTRA_OTHER_PACKAGE_NAME)
            ?: intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
        fail(attempt, label, uri, failure, packageToRemove)
    }

    private fun writeAndCommit(request: InstallRequest, attempt: InstallSessions.Attempt) {
        var sessionId = NO_SESSION
        try {
            if (!sessions.isCurrent(attempt)) return
            val length = apkLength(request.uri)
            sessionId = reservingSpace { packageInstaller.createSession(sessionParams(length)) }
            // Cancellation may have replaced this attempt while the session was created.
            if (!sessions.attach(attempt, sessionId)) {
                abandon(sessionId)
                return
            }
            packageInstaller.openSession(sessionId).use { session ->
                val input = context.contentResolver.openInputStream(request.uri)
                    ?: throw IllegalStateException("No stream for ${request.uri}")
                val written = input.use {
                    reservingSpace { session.openWrite(APK_NAME, 0, length) }.use { output ->
                        write(attempt, input, output, length).also { written ->
                            if (written) session.fsync(output)
                        }
                    }
                }
                // Otherwise it was cancelled, and the cancellation abandons the session itself
                if (written && sessions.update(attempt, InstallStatus.Installing)) {
                    committed.put(request.key, sessionId)
                    session.commit(resultIntent(sessionId, request).intentSender)
                }
            }
        } catch (ex: Exception) {
            if (sessionId != NO_SESSION) {
                abandon(sessionId)
            }
            // Cancelling pulls the session from under the copy, which is not a failure
            sessions.withCurrent(attempt) {
                analytics.trackException(ex, mapOf("reason" to "Install session failed"))
                val failure = if (ex is NoSpaceException) InstallFailure.NO_SPACE else InstallFailure.UNKNOWN
                fail(attempt, request.label, request.uri, failure, packageToRemove = null)
            }
        }
    }

    /** The system reserves the whole size up front, and that is what fails when it can't. */
    private inline fun <T> reservingSpace(reserve: () -> T): T {
        try {
            return reserve()
        } catch (ex: IOException) {
            throw NoSpaceException(ex)
        }
    }

    /** Returns false if the install was cancelled along the way. */
    private fun write(
        attempt: InstallSessions.Attempt,
        input: InputStream,
        output: OutputStream,
        length: Long,
    ): Boolean {
        val buffer = ByteArray(BUFFER_SIZE)
        var written = 0L
        var lastPercent = -1
        while (true) {
            if (!sessions.isCurrent(attempt)) {
                return false
            }
            val read = input.read(buffer)
            if (read < 0) {
                return true
            }
            output.write(buffer, 0, read)
            written += read
            if (length > 0) {
                val percent = (written * 100 / length).toInt().coerceAtMost(100)
                if (percent != lastPercent) {
                    lastPercent = percent
                    if (!sessions.update(attempt, InstallStatus.Writing(percent))) return false
                }
            }
        }
    }

    private fun sessionParams(length: Long): PackageInstaller.SessionParams {
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        if (length > 0) {
            params.setSize(length)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Keeps Android's restricted settings guard on community builds: without this
            // a session install loses the protection the system installer's route gives them
            params.setPackageSource(PackageInstaller.PACKAGE_SOURCE_DOWNLOADED_FILE)
        }
        return params
    }

    private fun apkLength(uri: Uri): Long {
        return context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
            ?: UNKNOWN_LENGTH
    }

    private fun resultIntent(sessionId: Int, request: InstallRequest): PendingIntent {
        val intent = Intent(context, InstallResultReceiver::class.java)
            .putExtra(EXTRA_KEY, request.key)
            .putExtra(EXTRA_LABEL, request.label)
            .putExtra(EXTRA_URI, request.uri)
            .putExtra(PackageInstaller.EXTRA_SESSION_ID, sessionId)
        // The system adds the result to this intent, so it has to stay mutable
        val mutable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }
        return PendingIntent.getBroadcast(
            context,
            sessionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or mutable
        )
    }

    private fun requestConfirmation(attempt: InstallSessions.Attempt, label: String, confirmation: Intent) {
        sessions.withCurrent(attempt) {
            // Already marked as installing since the commit, which is what it is once shown
            if (!activities.start(confirmation)) {
                confirmations[attempt] = confirmation
                sessions.update(attempt, InstallStatus.AwaitingConfirmation)
                notifications.showInstallConfirmation(attempt.key, label, confirmation)
            }
        }
    }

    private fun fail(
        attempt: InstallSessions.Attempt,
        label: String,
        uri: Uri?,
        failure: InstallFailure,
        packageToRemove: String?,
    ) {
        sessions.withCurrent(attempt) {
            analytics.trackEvent("install-failure-${failure.name.lowercase()}")
            val details = createInstallFailureIntent(context, attempt.key, label, uri, failure, packageToRemove)
            // A copy fails on its own thread, and screens are opened on the main one
            mainHandler.post {
                // A retry begun meanwhile makes this failure old news
                if (sessions.current(attempt.key) != null) return@post
                if (!activities.start(details)) {
                    notifications.showInstallFailure(
                        attempt.key,
                        label,
                        context.getString(failure.message),
                        details,
                    )
                }
            }
            finish(attempt)
        }
    }

    private fun finish(attempt: InstallSessions.Attempt) {
        confirmations.remove(attempt)
        if (sessions.finish(attempt)) {
            committed.remove(attempt.key)
        }
    }

    /**
     * Sessions outlive the process: the committed ones are the system's to answer and
     * are followed again, and any other holds a copy of its APK nobody will hand over.
     */
    private fun restoreSessions() {
        committed.all().forEach { (key, id) ->
            if (packageInstaller.getSessionInfo(id) != null) {
                sessions.restore(key, id)
            } else {
                committed.remove(key)
            }
        }
        val followed = sessions.ids()
        packageInstaller.mySessions
            .filter { it.sessionId !in followed }
            .forEach { abandon(it.sessionId) }
    }

    private fun abandon(sessionId: Int) {
        try {
            packageInstaller.abandonSession(sessionId)
        } catch (_: SecurityException) {
            // Thrown for a session the system has already finished with, which is
            // all abandoning was meant to achieve
        }
    }

}

private class NoSpaceException(cause: IOException) : IOException(cause)

private const val APK_NAME = "base.apk"
private const val BUFFER_SIZE = 1024 * 1024
private const val NO_SESSION = -1
private const val UNKNOWN_LENGTH = -1L
private const val EXTRA_KEY = "key"
private const val EXTRA_LABEL = "label"
private const val EXTRA_URI = "uri"
