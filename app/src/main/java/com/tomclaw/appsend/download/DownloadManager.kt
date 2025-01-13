package com.tomclaw.appsend.download

import com.jakewharton.rxrelay3.BehaviorRelay
import com.tomclaw.appsend.util.safeClose
import io.reactivex.rxjava3.core.Observable
import okhttp3.CookieJar
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

interface DownloadManager {

    fun status(appId: String): Observable<Int>

    fun download(label: String, version: String, appId: String, url: String): File

    fun targetFile(label: String, version: String, appId: String): File

    fun cancel(appId: String)

}


class DownloadManagerImpl(
    private val dir: File,
    private val cookieJar: CookieJar,
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
            println("[download] Finally status relay")
            if (relay.hasObservers()) {
                println("[download] Relay $appId has observers")
                return@doFinally
            }
            val inactiveState = relay.hasValue() &&
                    (relay.value == IDLE || relay.value == COMPLETED || relay.value == ERROR)
            println("[download] Relay $appId is inactive: $inactiveState")
            if (!relay.hasValue() || inactiveState) {
                relays.remove(appId)
                println("[download] Relay $appId removed")
            }
        }
    }

    override fun download(label: String, version: String, appId: String, url: String): File {
        val tmpFile = tempFile(label, version, appId)
        val targetFile = targetFile(label, version, appId)
        val relay = relays[appId] ?: BehaviorRelay.create()
        if (targetFile.exists()) {
            relay.accept(COMPLETED)
            return targetFile
        }
        relay.accept(AWAIT)
        downloads[appId] = executor.submit {
            relay.accept(STARTED)
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
            downloads.remove(appId)
        }
        relays[appId] = relay
        return targetFile
    }

    override fun targetFile(label: String, version: String, appId: String): File {
        val fileName = fileName(label, version, appId)
        return File(dir, "$fileName.apk")
    }

    private fun tempFile(label: String, version: String, appId: String): File {
        val fileName = fileName(label, version, appId)
        return File(dir, "$fileName.apk.tmp")
    }

    private fun fileName(label: String, version: String, appId: String): String {
        return escapeFileSymbols("$label-$version-$appId")
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
            println("[download] " + String.format("Download app url: %s", url))
            val u = URL(url)
            connection = u.openConnection() as HttpURLConnection

            val httpUrl = url.toHttpUrlOrNull()
                ?: throw IllegalArgumentException("Invalid upload screenshot URL")

            val cookies = cookieJar.loadForRequest(httpUrl)
                .map { it.toString() }
                .takeIf { it.isNotEmpty() }
                ?.reduce { acc, cookie -> "$acc;$cookie" }

            with(connection) {
                setRequestProperty("Cookie", cookies)
                connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
                requestMethod = GET
                useCaches = false
                doInput = true
                doOutput = true
                instanceFollowRedirects = false
            }
            connection.connect()
            val responseCode = connection.responseCode
            input = if (responseCode >= SC_BAD_REQUEST) {
                BufferedInputStream(connection.errorStream)
            } else {
                BufferedInputStream(connection.inputStream)
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
            var cache: Int
            var read: Long = 0
            var percent = 0
            var progressUpdateTime = 0L
            val buffer = ByteArray(BUFFER_SIZE)
            while (input.read(buffer).also { cache = it } != -1) {
                output.write(buffer, 0, cache)
                output.flush()
                read += cache.toLong()
                val p = (100 * read / total).toInt()
                if (p > percent) {
                    if (System.currentTimeMillis() > progressUpdateTime + 300) {
                        progressCallback(percent)
                        progressUpdateTime = System.currentTimeMillis()
                    }
                    percent = p
                }
                if (percent % 5 == 0) {
                    Thread.sleep(1)
                }
            }
            progressCallback(100)
            return true
        } catch (ex: InterruptedIOException) {
            println("[download] IO interruption while application downloading\n$ex")
        } catch (ex: InterruptedException) {
            println("[download] Interruption while application downloading\n$ex")
        } catch (ex: Throwable) {
            println("[download] Exception while application downloading\n$ex")
            errorCallback(ex)
        } finally {
            connection?.disconnect()
            input.safeClose()
            output.safeClose()
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

const val GET = "GET"
const val SC_BAD_REQUEST = 400

const val IDLE: Int = -30
const val AWAIT: Int = -10
const val STARTED: Int = -20
const val COMPLETED: Int = 101
const val ERROR: Int = -40

private const val BUFFER_SIZE = 1 * 1024 * 1024

private val RESERVED_CHARS = arrayOf("|", "\\", "/", "?", "*", "<", "\"", ":", ">")
