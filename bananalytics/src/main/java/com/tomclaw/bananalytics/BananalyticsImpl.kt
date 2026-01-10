package com.tomclaw.bananalytics

import android.util.Log
import com.google.gson.Gson
import com.tomclaw.bananalytics.api.AnalyticsEvent
import com.tomclaw.bananalytics.api.BreadcrumbCategory
import com.tomclaw.bananalytics.api.CrashReport
import com.tomclaw.bananalytics.api.SubmitCrashesRequest
import com.tomclaw.bananalytics.api.SubmitEventsRequest
import com.tomclaw.bananalytics.internal.ApiClient
import com.tomclaw.bananalytics.internal.BreadcrumbBuffer
import com.tomclaw.bananalytics.internal.CrashHandler
import com.tomclaw.bananalytics.internal.EventFileComparator
import com.tomclaw.bananalytics.internal.EventStorage
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Collections
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class BananalyticsImpl(
    filesDir: File,
    private val config: BananalyticsConfig,
    private val environmentProvider: EnvironmentProvider,
    private val isDebug: Boolean = false
) : Bananalytics {

    private val sessionId: String = UUID.randomUUID().toString()
    private val gson = Gson()
    private val storage = EventStorage(filesDir, gson)
    private val breadcrumbBuffer = BreadcrumbBuffer()
    private val apiClient = ApiClient(config, gson)
    private val executor: Executor = Executors.newSingleThreadExecutor()

    private val crashHandler = CrashHandler(
        sessionId = sessionId,
        storage = storage,
        breadcrumbBuffer = breadcrumbBuffer,
        environmentProvider = { environmentProvider.environment() }
    )

    // === Lifecycle ===

    override fun install() {
        Thread.setDefaultUncaughtExceptionHandler(crashHandler)
        log("Crash handler installed")

        executor.execute {
            sendPendingCrashes()
            sendPendingEvents()
        }
    }

    // === Events ===

    override fun trackEvent(name: String) =
        trackEvent(name, tags = emptyMap(), fields = emptyMap())

    override fun trackEvent(name: String, key: String, value: String) =
        trackEvent(name, tags = mapOf(key to value), fields = emptyMap())

    override fun trackEvent(name: String, key: String, value: Double) =
        trackEvent(name, tags = emptyMap(), fields = mapOf(key to value))

    override fun trackEvent(
        name: String,
        tags: Map<String, String>,
        fields: Map<String, Double>
    ) {
        executor.execute {
            val event = AnalyticsEvent(
                sessionId = sessionId,
                name = name.replace("-", "_"),
                tags = tags,
                fields = fields,
                time = System.currentTimeMillis()
            )
            storage.writeEvent(event)
            sendPendingEvents()
        }
    }

    override fun flushEvents() {
        executor.execute {
            sendPendingEvents()
        }
    }

    // === Crashes ===

    override fun trackException(throwable: Throwable, context: Map<String, String>) {
        executor.execute {
            val crash = CrashReport(
                sessionId = sessionId,
                timestamp = System.currentTimeMillis(),
                threadName = Thread.currentThread().name,
                stacktrace = throwable.toStackTraceString(),
                isFatal = false,
                context = context,
                breadcrumbs = breadcrumbBuffer.snapshot()
            )
            storage.writeCrashSync(crash)
            log("Non-fatal exception tracked: ${throwable.message}")
            sendPendingCrashes()
        }
    }

    // === Breadcrumbs ===

    override fun leaveBreadcrumb(message: String, category: BreadcrumbCategory) {
        breadcrumbBuffer.add(message, category)
    }

    // === Private ===

    private fun sendPendingCrashes() {
        val crashFiles = storage.listCrashFiles()
        if (crashFiles.isEmpty()) return

        val crashes = crashFiles.mapNotNull { storage.readCrash(it) }
        if (crashes.isEmpty()) return

        log("Sending ${crashes.size} pending crashes")

        try {
            val request = SubmitCrashesRequest(
                sessionId = sessionId,
                environment = environmentProvider.environment(),
                crashes = crashes
            )
            val success = apiClient.sendCrashes(request)
            if (success) {
                storage.deleteFiles(crashFiles)
                log("Crashes sent successfully")
            } else {
                log("Failed to send crashes")
            }
        } catch (e: IOException) {
            log("Network error while sending crashes: ${e.message}")
        } catch (e: Throwable) {
            log("Error sending crashes: ${e.message}")
        }
    }

    private fun sendPendingEvents() {
        val files = storage.listEventFiles().toMutableList()
        if (files.size < BATCH_SIZE) return

        Collections.sort(files, EventFileComparator())

        val events = mutableListOf<AnalyticsEvent>()
        val filesToRemove = mutableListOf<File>()

        try {
            while (files.isNotEmpty() && files.size + filesToRemove.size >= BATCH_SIZE) {
                val file = files.removeAt(0)
                storage.readEvent(file)?.let { events.add(it) }
                filesToRemove.add(file)

                if (events.size >= BATCH_SIZE) {
                    log("Sending batch of ${events.size} events")
                    try {
                        val request = SubmitEventsRequest(
                            sessionId = sessionId,
                            environment = environmentProvider.environment(),
                            events = events
                        )
                        val success = apiClient.sendEvents(request)
                        if (success) {
                            storage.deleteFiles(filesToRemove)
                            log("Events batch sent successfully")
                        } else {
                            log("Failed to send events batch")
                            return
                        }
                    } catch (e: IOException) {
                        log("Network error while sending events: ${e.message}")
                        return
                    } catch (e: Throwable) {
                        log("Error sending events - skipping batch: ${e.message}")
                    }
                    events.clear()
                    filesToRemove.clear()
                }
            }
        } catch (e: Throwable) {
            log("Error in sendPendingEvents: ${e.message}")
        }
    }

    private fun Throwable.toStackTraceString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }

    private fun log(message: String) {
        if (isDebug) {
            Log.d(LOG_TAG, message)
        }
    }

    companion object {
        private const val LOG_TAG = "bananalytics"
        private const val BATCH_SIZE = 20
    }
}
