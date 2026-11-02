package com.tomclaw.appsend.install

import android.app.ActivityManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.tomclaw.appsend.download.DownloadNotifications
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.getParcelableExtraCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * Installs through a [PackageInstaller] session, which makes Appteka the installer
 * of record. That is what lets later updates skip the system's confirmation where
 * Android allows it; every other case comes back as a confirmation to show.
 */
class SessionInstallMethod(
    private val context: Context,
    tracker: InstallTracker,
    private val notifications: DownloadNotifications,
    private val analytics: Analytics,
) : InstallMethod {

    private val packageInstaller = context.packageManager.packageInstaller
    private val executor = Executors.newSingleThreadExecutor()

    private val sessions = InstallSessions(tracker)

    // Kept until the final answer: a question asked while the app was away may never
    // have reached the user, as notifications can be off, and a new tap asks it again
    private val confirmations = ConcurrentHashMap<InstallSessions.Attempt, Intent>()

    private var staleSessionsAbandoned = false

    override fun install(request: InstallRequest) {
        val attempt = sessions.start(request.key)
        if (attempt == null) {
            sessions.current(request.key)?.let { active ->
                confirmations[active]?.let { requestConfirmation(active, request.label, it) }
            }
            return
        }
        sessions.withCurrent(attempt) {
            notifications.cancelInstall(request.key)
            startService(request)
        }
        executor.execute { writeAndCommit(request, attempt) }
    }

    private fun startService(request: InstallRequest) {
        try {
            ContextCompat.startForegroundService(
                context,
                createInstallServiceIntent(context, request)
            )
        } catch (ex: IllegalStateException) {
            // Refused if the user has left in this very moment. The copy still runs,
            // as any app's work does in the background, just without the protection
            analytics.trackException(ex, mapOf("reason" to "Install service not started"))
        }
    }

    override fun cancel(key: String) {
        val attempt = sessions.current(key) ?: return
        val cancelled = sessions.withCurrent(attempt) {
            confirmations.remove(attempt)
            notifications.cancelInstall(key)
            sessions.finish(attempt)
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
            abandonStaleSessions()
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
            confirmations[attempt] = confirmation
            sessions.update(attempt, InstallStatus.Confirming)
            // Android refuses to open a screen from the background without saying so, and it
            // can do that even while the app still counts as visible, right after the user
            // has left. So the notification is always there, and only stays quiet when the
            // screen is likely to have opened; the final answer takes it away.
            val visible = isAppVisible()
            notifications.showInstallConfirmation(attempt.key, label, confirmation, silent = visible)
            if (visible) {
                context.startActivity(confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
            // The same reasoning as for the confirmation: the dialog takes this away once it opens
            val visible = isAppVisible()
            notifications.showInstallFailure(
                attempt.key,
                label,
                context.getString(failure.message),
                details,
                silent = visible
            )
            if (visible) {
                context.startActivity(details.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            finish(attempt)
        }
    }

    private fun finish(attempt: InstallSessions.Attempt) {
        confirmations.remove(attempt)
        sessions.finish(attempt)
    }

    private fun isAppVisible(): Boolean {
        val state = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(state)
        return state.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    /** Sessions outlive the process, and each holds a full copy of its APK for days. */
    private fun abandonStaleSessions() {
        if (staleSessionsAbandoned) {
            return
        }
        staleSessionsAbandoned = true
        val activeIds = sessions.ids()
        packageInstaller.mySessions
            .filter { it.sessionId !in activeIds }
            .filter { !mayStillBeAsking(it) }
            .forEach { abandon(it.sessionId) }
    }

    /**
     * The system's question can still be on screen, and abandoning its session would leave
     * a dialog whose button does nothing. After a while nobody is coming back to answer it.
     */
    private fun mayStillBeAsking(session: PackageInstaller.SessionInfo): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return false
        }
        val age = System.currentTimeMillis() - session.createdMillis
        return session.isCommitted && age < UNANSWERED_SESSION_TTL_MS
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
private const val UNANSWERED_SESSION_TTL_MS = 60 * 60 * 1000L
private const val EXTRA_KEY = "key"
private const val EXTRA_LABEL = "label"
private const val EXTRA_URI = "uri"
