package com.tomclaw.bananalytics.internal

import com.tomclaw.bananalytics.api.CrashReport
import com.tomclaw.bananalytics.api.Environment
import java.io.PrintWriter
import java.io.StringWriter

internal class CrashHandler(
    private val sessionId: String,
    private val storage: EventStorage,
    private val breadcrumbBuffer: BreadcrumbBuffer,
    private val environmentProvider: () -> Environment
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler: Thread.UncaughtExceptionHandler? =
        Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val crash = CrashReport(
                sessionId = sessionId,
                timestamp = System.currentTimeMillis(),
                threadName = thread.name,
                stacktrace = throwable.toStackTraceString(),
                isFatal = true,
                context = emptyMap(),
                breadcrumbs = breadcrumbBuffer.snapshot()
            )
            storage.writeCrashSync(crash)
        } catch (_: Throwable) {
            // Ignore - never cause a secondary crash
        }

        // Always pass to the default handler
        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun Throwable.toStackTraceString(): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        printStackTrace(pw)
        pw.flush()
        return sw.toString()
    }
}
