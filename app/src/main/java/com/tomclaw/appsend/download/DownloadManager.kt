package com.tomclaw.appsend.download

import android.net.Uri
import com.jakewharton.rxrelay3.BehaviorRelay
import com.tomclaw.appsend.util.safeClose
import io.reactivex.rxjava3.core.Observable
import okhttp3.CookieJar
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.BufferedInputStream
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

    fun download(label: String, version: String, appId: String, url: String): String

    fun getInstallUri(label: String, version: String, appId: String): Uri?

    fun exists(label: String, version: String, appId: String): Boolean

    fun cancel(appId: String)

}


class DownloadManagerImpl(
    private val apkStorage: ApkStorage,
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

    override fun download(label: String, version: String, appId: String, url: String): String {
        val fileName = fileName(label, version, appId)
        val relay = relays[appId] ?: BehaviorRelay.create()

        if (apkStorage.exists(fileName)) {
            relay.accept(COMPLETED)
            relays[appId] = relay
            return fileName
        }

        relay.accept(AWAIT)
        downloads[appId] = executor.submit {
            relay.accept(STARTED)
            val result = downloadBlocking(
                url = url,
                fileName = fileName,
                progressCallback = { percent ->
                    relay.accept(percent)
                },
                errorCallback = {
                    relay.accept(ERROR)
                },
            )
            when (result) {
                DownloadResult.SUCCESS -> {
                    apkStorage.commit(fileName)
                    relay.accept(COMPLETED)
                }
                DownloadResult.INTERRUPTED -> {
                    // Keep tmp file for resume - don't delete
                    relay.accept(IDLE)
                }
                DownloadResult.ERROR -> {
                    // Keep tmp file for resume - don't delete
                    // relay already has ERROR from errorCallback
                }
            }
            downloads.remove(appId)
        }
        relays[appId] = relay
        return fileName
    }

    override fun getInstallUri(label: String, version: String, appId: String): Uri? {
        val fileName = fileName(label, version, appId)
        return apkStorage.getInstallUri(fileName)
    }

    override fun exists(label: String, version: String, appId: String): Boolean {
        val fileName = fileName(label, version, appId)
        return apkStorage.exists(fileName)
    }

    private fun fileName(label: String, version: String, appId: String): String {
        return escapeFileSymbols("$label-$version-$appId")
    }

    override fun cancel(appId: String) {
        downloads.remove(appId)?.cancel(true)
        // Keep tmp file for resume - don't delete
        relays[appId]?.accept(IDLE)
    }

    private fun downloadBlocking(
        url: String,
        fileName: String,
        progressCallback: (Int) -> Unit,
        errorCallback: (Throwable) -> Unit
    ): DownloadResult {
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            println("[download] " + String.format("Download app url: %s", url))
            val u = URL(url)
            connection = u.openConnection() as HttpURLConnection

            val httpUrl = url.toHttpUrlOrNull()
                ?: throw IllegalArgumentException("Invalid download URL")

            val cookies = cookieJar.loadForRequest(httpUrl)
                .map { it.toString() }
                .takeIf { it.isNotEmpty() }
                ?.reduce { acc, cookie -> "$acc;$cookie" }

            // Check for existing partial file for resume
            val downloadedBytes = apkStorage.getTmpSize(fileName)
            println("[download] Existing partial file size: $downloadedBytes bytes")

            with(connection) {
                setRequestProperty("Cookie", cookies)
                connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
                requestMethod = GET
                useCaches = false
                doInput = true
                instanceFollowRedirects = false
                
                // Request resume if partial file exists
                if (downloadedBytes > 0) {
                    setRequestProperty("Range", "bytes=$downloadedBytes-")
                    println("[download] Requesting resume from byte $downloadedBytes")
                }
            }
            connection.connect()
            val responseCode = connection.responseCode
            
            // HTTP 206 = Partial Content (server supports resume)
            // HTTP 200 = OK (server doesn't support resume, start from beginning)
            val isResumable = responseCode == SC_PARTIAL_CONTENT
            val startByte = if (isResumable) downloadedBytes else 0L
            
            if (isResumable) {
                println("[download] Server supports resume, continuing from byte $startByte")
            } else if (downloadedBytes > 0) {
                println("[download] Server doesn't support resume (HTTP $responseCode), starting from beginning")
            }
            
            if (responseCode >= SC_BAD_REQUEST) {
                input = BufferedInputStream(connection.errorStream)
                errorCallback(IOException("HTTP error: $responseCode"))
                return DownloadResult.ERROR
            }
            
            input = BufferedInputStream(connection.inputStream)
            
            // Get total size: Content-Length for 200, Content-Range for 206
            val contentLength = connection.contentLength.toLong()
            val total = if (isResumable) {
                // Parse Content-Range: "bytes 1000-9999/10000"
                connection.getHeaderField("Content-Range")
                    ?.substringAfter("/")?.toLongOrNull()
                    ?: (startByte + contentLength)
            } else {
                contentLength
            }
            
            if (total <= 0) {
                errorCallback(IOException("ContentLength is not defined"))
                return DownloadResult.ERROR
            }

            // Open for writing or appending
            output = if (isResumable && startByte > 0) {
                apkStorage.openAppend(fileName)
            } else {
                apkStorage.openWrite(fileName)
            }

            var cache: Int
            var read = startByte
            var percent = (100 * read / total).toInt()
            var progressUpdateTime = 0L
            val buffer = ByteArray(BUFFER_SIZE)
            
            // Report initial progress for resumed downloads
            if (startByte > 0) {
                progressCallback(percent)
            }
            
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
            }
            progressCallback(100)
            return DownloadResult.SUCCESS
        } catch (ex: InterruptedIOException) {
            println("[download] IO interruption - partial file saved for resume\n$ex")
            return DownloadResult.INTERRUPTED
        } catch (ex: InterruptedException) {
            println("[download] Interrupted - partial file saved for resume\n$ex")
            return DownloadResult.INTERRUPTED
        } catch (ex: Throwable) {
            println("[download] Exception while application downloading\n$ex")
            errorCallback(ex)
            return DownloadResult.ERROR
        } finally {
            connection?.disconnect()
            input.safeClose()
            output.safeClose()
        }
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
const val SC_PARTIAL_CONTENT = 206

const val IDLE: Int = -30
const val AWAIT: Int = -10
const val STARTED: Int = -20
const val COMPLETED: Int = 101
const val ERROR: Int = -40

private const val BUFFER_SIZE = 1 * 1024 * 1024

private val RESERVED_CHARS = arrayOf("|", "\\", "/", "?", "*", "<", "\"", ":", ">")

enum class DownloadResult {
    SUCCESS,
    INTERRUPTED,
    ERROR
}
