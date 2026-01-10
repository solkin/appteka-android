package com.tomclaw.bananalytics.internal

import com.google.gson.Gson
import com.tomclaw.bananalytics.api.AnalyticsEvent
import com.tomclaw.bananalytics.api.CrashReport
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

internal class EventStorage(
    filesDir: File,
    private val gson: Gson
) {

    private val eventsDir = File(filesDir, EVENTS_DIR).apply { mkdirs() }
    private val crashesDir = File(filesDir, CRASHES_DIR).apply { mkdirs() }

    // === Events ===

    fun writeEvent(event: AnalyticsEvent): File {
        val file = File(eventsDir, generateEventFileName(event.name, event.time))
        try {
            FileOutputStream(file).use { output ->
                val json = gson.toJson(event)
                output.write(json.toByteArray(Charsets.UTF_8))
                output.flush()
            }
        } catch (_: IOException) {
            file.delete()
        }
        return file
    }

    fun readEvent(file: File): AnalyticsEvent? {
        return try {
            FileInputStream(file).use { input ->
                val json = input.readBytes().toString(Charsets.UTF_8)
                gson.fromJson(json, AnalyticsEvent::class.java)
            }
        } catch (_: Throwable) {
            file.delete()
            null
        }
    }

    fun listEventFiles(): List<File> {
        return eventsDir.listFiles()?.toList() ?: emptyList()
    }

    // === Crashes ===

    fun writeCrashSync(crash: CrashReport) {
        val file = File(crashesDir, generateCrashFileName(crash.timestamp, crash.isFatal))
        try {
            FileOutputStream(file).use { output ->
                val json = gson.toJson(crash)
                output.write(json.toByteArray(Charsets.UTF_8))
                output.flush()
            }
        } catch (_: IOException) {
            file.delete()
        }
    }

    fun readCrash(file: File): CrashReport? {
        return try {
            FileInputStream(file).use { input ->
                val json = input.readBytes().toString(Charsets.UTF_8)
                gson.fromJson(json, CrashReport::class.java)
            }
        } catch (_: Throwable) {
            file.delete()
            null
        }
    }

    fun listCrashFiles(): List<File> {
        return crashesDir.listFiles()?.toList() ?: emptyList()
    }

    fun deleteFiles(files: List<File>) {
        files.forEach { it.delete() }
    }

    private fun generateEventFileName(event: String, time: Long): String {
        return "$time-${event.md5()}.event"
    }

    private fun generateCrashFileName(time: Long, isFatal: Boolean): String {
        val type = if (isFatal) "fatal" else "exception"
        return "$time-$type.crash"
    }

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val EVENTS_DIR = "bananalytics/events"
        private const val CRASHES_DIR = "bananalytics/crashes"
    }
}

internal fun getFileNameTime(fileName: String): Long {
    val timeDivider = fileName.indexOf('-')
    return if (timeDivider > 0) {
        fileName.substring(0, timeDivider).toLongOrNull() ?: 0L
    } else 0L
}

internal class EventFileComparator : Comparator<File> {

    override fun compare(o1: File, o2: File): Int {
        return getFileNameTime(o1.name).compareTo(getFileNameTime(o2.name))
    }
}
