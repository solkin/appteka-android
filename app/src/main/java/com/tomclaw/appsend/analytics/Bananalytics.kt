package com.tomclaw.appsend.analytics

import com.tomclaw.appsend.analytics.api.AnalyticsEvent
import com.tomclaw.appsend.analytics.api.SubmitEventsRequest
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.util.Logger
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Collections
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface Bananalytics {

    fun trackEvent(name: String)

    fun trackEvent(name: String, key: String, value: String, isImmediate: Boolean)

    fun trackEvent(name: String, key: String, value: Double, isImmediate: Boolean)

    fun trackEvent(
        name: String,
        tags: Map<String, String> = emptyMap(),
        fields: Map<String, Double> = emptyMap(),
        isImmediate: Boolean = false
    )

    fun flushEvents()

}

class BananalyticsImpl(
    private val filesDir: File,
    private val environmentProvider: EnvironmentProvider,
    private val api: StoreApi,
    private val logger: Logger,
) : Bananalytics {

    private val executor: Executor = Executors.newSingleThreadExecutor()

    @Suppress("unused")
    override fun trackEvent(name: String) =
        trackEvent(name, tags = emptyMap(), fields = emptyMap(), isImmediate = false)

    @Suppress("unused")
    override fun trackEvent(name: String, key: String, value: String, isImmediate: Boolean) =
        trackEvent(name, tags = mapOf(Pair(key, value)), fields = emptyMap(), isImmediate)

    @Suppress("unused")
    override fun trackEvent(name: String, key: String, value: Double, isImmediate: Boolean) =
        trackEvent(name, tags = emptyMap(), fields = mapOf(Pair(key, value)), isImmediate)

    @Suppress("unused")
    override fun trackEvent(
        name: String,
        tags: Map<String, String>,
        fields: Map<String, Double>,
        isImmediate: Boolean
    ) {
        executor.execute {
            val file = writeEvent(createEvent(name.replace("-", "_"), tags, fields))
            if (isImmediate) {
                sendEventImmediate(file)
            } else {
                flushEventsSync()
            }
        }
    }

    @Suppress("unused")
    override fun flushEvents() {
        executor.execute {
            flushEventsSync()
        }
    }

    private fun flushEventsSync() {
        val files = eventsFiles().toMutableList()
        sendEvents(files)
    }

    private fun writeEvent(event: AnalyticsEvent): File {
        val file = File(eventsDir(), generateEventFileName(event.name, event.time))
        try {
            val version: Byte = 1
            DataOutputStream(FileOutputStream(file)).use { output ->
                output.writeByte(version.toInt())
                output.writeUTF(event.name)

                output.writeInt(event.tags.size)
                for (tag in event.tags) {
                    output.writeUTF(tag.key)
                    output.writeUTF(tag.value)
                }

                output.writeInt(event.fields.size)
                for (tag in event.fields) {
                    output.writeUTF(tag.key)
                    output.writeDouble(tag.value)
                }

                output.writeLong(event.time)

                output.flush()
            }
        } catch (_: IOException) {
            file.delete()
        }
        return file
    }

    private fun readEvent(file: File): AnalyticsEvent? {
        try {
            DataInputStream(FileInputStream(file)).use { input ->
                val version = input.readByte()
                if (version.toInt() == 1) {
                    val name = input.readUTF()

                    val tags = input.readInt().takeIf { it > 0 }?.let { tagsCount ->
                        val tags = mutableMapOf<String, String>()
                        for (i in 0 until tagsCount) {
                            val key = input.readUTF()
                            val value = input.readUTF()
                            tags[key] = value
                        }
                        tags
                    } ?: emptyMap()

                    val fields = input.readInt().takeIf { it > 0 }?.let { fieldsCount ->
                        val fields = mutableMapOf<String, Double>()
                        for (i in 0 until fieldsCount) {
                            val key = input.readUTF()
                            val value = input.readDouble()
                            fields[key] = value
                        }
                        fields
                    } ?: emptyMap()

                    val time = input.readLong()

                    return createEvent(name, tags, fields, time)
                }
            }
        } catch (_: Throwable) {
            file.delete()
        }
        return null
    }

    private fun sendEventImmediate(file: File) {
        sendEvents(mutableListOf(file), 1)
    }

    private fun sendEvents(files: MutableList<File>, batchSize: Int = BATCH_SIZE) {
        if (files.size >= batchSize) {
            Collections.sort(files, EventFileComparator())
            val events: MutableList<AnalyticsEvent> = ArrayList()
            val filesToRemove: MutableList<File> = ArrayList()
            try {
                do {
                    val file: File = files.removeAt(0)
                    readEvent(file)?.let { events.add(it) }
                    filesToRemove.add(file)
                    if (events.size >= batchSize) {
                        log("events data: $events")
                        try {
                            val request = SubmitEventsRequest(
                                environment = environmentProvider.environment(),
                                events = events
                            )
                            val result = api.submitEvents(request).blockingGet()
                            log("batch result: $result")
                        } catch (ex: Throwable) {
                            val cause = ex.cause ?: ex
                            try {
                                throw (cause)
                            } catch (ex: IOException) {
                                log("network error while sending analytics track")
                                return
                            } catch (ex: Throwable) {
                                log("failed to send analytics event - skipping")
                            }
                        }
                        for (f in filesToRemove) {
                            f.delete()
                            log("remove event file: " + f.name)
                        }
                        events.clear()
                        filesToRemove.clear()
                    }
                } while (files.size + filesToRemove.size >= batchSize)
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    private fun eventsDir(): File {
        val dir = File(filesDir, EVENTS_DIR)
        dir.mkdirs()
        return dir
    }

    private fun eventsFiles(): List<File> {
        val dir = eventsDir()
        return dir.listFiles()?.asList() ?: emptyList()
    }

    private fun createEvent(
        name: String,
        tags: Map<String, String> = emptyMap(),
        fields: Map<String, Double> = emptyMap(),
        time: Long = System.currentTimeMillis()
    ) = AnalyticsEvent(name, tags, fields, time)

    private fun log(s: String) {
        logger.log("[bananalytics] $s")
    }

}

private const val BATCH_SIZE = 20
private const val EVENTS_DIR = "bananalytics"
