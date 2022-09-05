package com.tomclaw.appsend.util

import android.util.Log
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.core.Observable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicIntegerArray

interface DownloadManager {

    fun status(appId: String): Observable<Int>

    fun download(label: String, version: String, appId: String, url: String): File

    fun cancel(appId: String)

}


class DownloadManagerImpl(
    private val dir: File,
) : DownloadManager {

    private val executor = Executors.newSingleThreadExecutor()

    private val relays = HashMap<String, BehaviorRelay<Int>>()
    private val downloads = HashMap<String, Future<*>>()

    override fun status(appId: String): Observable<Int> {
        val relay = relays[appId] ?: let {
            val relay = BehaviorRelay.createDefault(IDLE)
            relay.accept(IDLE)
            relays[appId] = relay
            relay
        }
        return relay.doFinally {
            LegacyLogger.log("Finally status relay")
            if (relay.hasObservers()) {
                LegacyLogger.log("Relay $appId has observers")
                return@doFinally
            }
            val inactiveState = relay.hasValue() &&
                    (relay.value == IDLE || relay.value == COMPLETED || relay.value == ERROR)
            LegacyLogger.log("Relay $appId is inactive: $inactiveState")
            if (!relay.hasValue() || inactiveState) {
                relays.remove(appId)
                LegacyLogger.log("Relay $appId removed")
            }
        }
    }

    override fun download(label: String, version: String, appId: String, url: String): File {
        val fileName = escapeFileSymbols("$label-$version-$appId")
        val tmpFile = File(dir, "$fileName.apk.tmp")
        val targetFile = File(dir, "$fileName.apk")
        val relay = relays[appId] ?: BehaviorRelay.create()
        if (targetFile.exists()) {
            relay.accept(COMPLETED)
            return targetFile
        }
        relay.accept(AWAIT)
        downloads[appId] = executor.submit {
            val success = downloadBlocking(
                url = url,
                file = tmpFile,
                progressCallback = { percent ->
                    relay.accept(percent)
                },
                errorCallback = {
                    relay.accept(ERROR)
                },
            )
            if (success) {
                tmpFile.renameTo(targetFile)
                relay.accept(COMPLETED)
            }
        }
        relays[appId] = relay
        return targetFile
    }

    override fun cancel(appId: String) {
        downloads.remove(appId)?.cancel(true)
        relays[appId]?.accept(IDLE)
    }

    private fun downloadBlocking(
        url: String,
        file: File,
        progressCallback: (Int) -> Unit,
        errorCallback: (Throwable) -> Unit
    ): Boolean {
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            LegacyLogger.log(String.format("Download app url: %s", url))
            val u = URL(url)
            connection = u.openConnection() as HttpURLConnection
            with(connection) {
                connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
                requestMethod = HttpUtil.GET
                useCaches = false
                doInput = true
                doOutput = true
                instanceFollowRedirects = false
            }
            connection.connect()
            val responseCode = connection.responseCode
            input = if (responseCode >= HttpUtil.SC_BAD_REQUEST) {
                connection.errorStream
            } else {
                connection.inputStream
            }
            val total = connection.contentLength
            if (total <= 0) {
                errorCallback(IOException("ContentLength is not defined"))
                return false
            }
            file.parentFile?.mkdirs()
            if (file.exists()) {
                file.delete()
            }
            output = FileOutputStream(file)
            val buffer = VariableBuffer()
            var cache: Int
            var read: Long = 0
            var percent = 0
            buffer.onExecuteStart()
            while (input.read(buffer.calculateBuffer()).also { cache = it } != -1) {
                buffer.onExecuteCompleted(cache)
                output.write(buffer.buffer, 0, cache)
                output.flush()
                read += cache.toLong()
                val p = (100 * read / total).toInt()
                if (p > percent) {
                    progressCallback(percent)
                    percent = p
                }
                buffer.onExecuteStart()
                Thread.sleep(10) // TODO: remove this slowing down
            }
            progressCallback(100)
            return true
        } catch (ex: InterruptedException) {
            LegacyLogger.log("Interruption while application downloading", ex)
        } catch (ex: Throwable) {
            LegacyLogger.log("Exception while application downloading", ex)
            errorCallback(ex)
        } finally {
            connection?.disconnect()
            HttpUtil.closeSafely(input)
            HttpUtil.closeSafely(output)
        }
        return false
    }

    private fun escapeFileSymbols(name: String): String {
        var fileName = name
        for (symbol in RESERVED_CHARS) {
            fileName = fileName.replace(symbol[0], '_')
        }
        return fileName
    }

}

const val IDLE: Int = -2
const val AWAIT: Int = -1
const val COMPLETED: Int = 101
const val ERROR: Int = -3

private val RESERVED_CHARS = arrayOf("|", "\\", "/", "?", "*", "<", "\"", ":", ">")
