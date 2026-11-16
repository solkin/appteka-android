package com.tomclaw.appsend.download

import com.jakewharton.rxrelay3.BehaviorRelay
import com.tomclaw.appsend.core.ProxyConfigProvider
import com.tomclaw.appsend.core.UserAgentProvider
import com.tomclaw.appsend.util.FileHelper.escapeFileSymbols
import com.tomclaw.appsend.util.logDebug
import com.tomclaw.appsend.util.safeClose
import com.tomclaw.appsend.util.sha1
import io.reactivex.rxjava3.core.Observable
import okhttp3.CookieJar
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InterruptedIOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

interface DownloadManager {

    fun status(appId: String): Observable<DownloadState>

    fun download(
        label: String,
        version: String,
        appId: String,
        url: String,
        sha1: String?,
    ): String

    fun exists(label: String, version: String, appId: String): Boolean

    fun cancel(appId: String)

}


class DownloadManagerImpl(
    private val apkStorage: ApkStorage,
    private val cookieJar: CookieJar,
    private val proxyConfigProvider: ProxyConfigProvider,
    private val userAgentProvider: UserAgentProvider,
) : DownloadManager {

    private val executor = Executors.newSingleThreadExecutor()

    // Touched from both the service (main thread) and the download executor
    private val relays = ConcurrentHashMap<String, BehaviorRelay<DownloadState>>()
    private val downloads = ConcurrentHashMap<String, Future<*>>()
    private val fileNames = ConcurrentHashMap<String, String>()

    /**
     * Resolved through putIfAbsent so a status subscriber and a starting download
     * racing on the same appId cannot end up holding two different relays — the
     * loser of that race would never see the download it is watching.
     */
    private fun relayFor(appId: String): BehaviorRelay<DownloadState> {
        relays[appId]?.let { return it }
        val created = BehaviorRelay.createDefault<DownloadState>(DownloadState.Idle)
        return relays.putIfAbsent(appId, created) ?: created
    }

    override fun status(appId: String): Observable<DownloadState> {
        val relay = relayFor(appId)
        return relay.doFinally {
            logDebug("[download] Finally status relay")
            if (relay.hasObservers()) {
                logDebug("[download] Relay $appId has observers")
                return@doFinally
            }
            val inactiveState = when (relay.value) {
                DownloadState.Idle, DownloadState.Failed, is DownloadState.Completed -> true
                DownloadState.Queued, is DownloadState.Running, null -> false
            }
            logDebug("[download] Relay $appId is inactive: $inactiveState")
            if (inactiveState) {
                relays.remove(appId)
                logDebug("[download] Relay $appId removed")
            }
        }
    }

    override fun download(
        label: String,
        version: String,
        appId: String,
        url: String,
        sha1: String?,
    ): String {
        val fileName = fileName(label, version, appId)
        val relay = relayFor(appId)

        apkStorage.getInstallUri(fileName)?.let { uri ->
            relay.accept(DownloadState.Completed(uri))
            return fileName
        }

        // Check if download is already in progress
        val existingDownload = downloads[appId]
        if (existingDownload != null && !existingDownload.isDone) {
            return fileName
        }

        relay.accept(DownloadState.Queued)
        fileNames[appId] = fileName
        downloads[appId] = executor.submit {
            try {
                relay.accept(DownloadState.Running(percent = null))
                val result = downloadBlocking(
                    url = url,
                    fileName = fileName,
                    sha1 = sha1,
                    progressCallback = { percent ->
                        relay.accept(DownloadState.Running(percent))
                    },
                    errorCallback = {
                        relay.accept(DownloadState.Failed)
                    },
                )
                when (result) {
                    DownloadResult.SUCCESS -> {
                        val uri = apkStorage.commit(fileName)
                        if (uri != null) {
                            fileNames.remove(appId)
                            relay.accept(DownloadState.Completed(uri))
                        } else {
                            relay.accept(DownloadState.Failed)
                        }
                    }
                    DownloadResult.INTERRUPTED -> {
                        // Keep tmp file for resume - don't delete
                        relay.accept(DownloadState.Idle)
                    }
                    DownloadResult.ERROR -> {
                        // Keep tmp file for resume - don't delete
                        // relay already has Failed from errorCallback
                    }
                }
            } catch (ex: Throwable) {
                // Escaping here would leave the relay without a terminal state,
                // and the foreground service would keep running until Android kills it
                logDebug("[download] Unexpected failure while downloading\n$ex")
                relay.accept(DownloadState.Failed)
            } finally {
                downloads.remove(appId)
            }
        }
        return fileName
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
        // Delete tmp file on explicit user cancel
        fileNames.remove(appId)?.let { fileName ->
            apkStorage.deleteTmp(fileName)
        }
        relays[appId]?.accept(DownloadState.Idle)
    }

    private fun downloadBlocking(
        url: String,
        fileName: String,
        sha1: String?,
        progressCallback: (Int) -> Unit,
        errorCallback: (Throwable) -> Unit
    ): DownloadResult {
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var output: OutputStream? = null
        try {
            val proxy: Proxy? = proxyConfigProvider.getProxyConfig().toProxy()

            // Check for existing partial file for resume
            var downloadedBytes = apkStorage.getTmpSize(fileName)

            connection = openConnection(url, downloadedBytes, proxy)

            // The partial file is no longer something to resume from: it already
            // covers the whole resource, or the resource changed under it. Only
            // an explicit cancel ever drops a tmp file, so without this the same
            // doomed range gets replayed on every retry, forever.
            if (connection.responseCode == SC_RANGE_NOT_SATISFIABLE && downloadedBytes > 0) {
                logDebug("[download] Partial file rejected with 416, starting over")
                connection.disconnect()
                apkStorage.deleteTmp(fileName)
                downloadedBytes = 0L
                connection = openConnection(url, downloadedBytes, proxy)
            }

            val responseCode = connection.responseCode
            
            // HTTP 206 = Partial Content (server supports resume)
            // HTTP 200 = OK (server doesn't support resume, start from beginning)
            val isResumable = responseCode == SC_PARTIAL_CONTENT
            val startByte = if (isResumable) downloadedBytes else 0L
            
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
                        progressCallback(p)
                        progressUpdateTime = System.currentTimeMillis()
                    }
                    percent = p
                }
            }
            // A stream can end early without raising anything, and committing
            // then would rename a truncated file to .apk — from that point the
            // app skips the download and the installer fails to parse it
            if (read < total) {
                errorCallback(IOException("Incomplete download: $read of $total bytes"))
                return DownloadResult.ERROR
            }
            // Push the last buffer out before the file gets read back
            output.safeClose()
            output = null
            // The length says nothing about a resume that started at a wrong
            // offset, or about bytes mangled on the way
            if (!matchesChecksum(fileName, sha1)) {
                apkStorage.deleteTmp(fileName)
                errorCallback(IOException("Checksum mismatch"))
                return DownloadResult.ERROR
            }
            progressCallback(100)
            return DownloadResult.SUCCESS
        } catch (ex: InterruptedIOException) {
            logDebug("[download] IO interruption - partial file saved for resume\n$ex")
            return DownloadResult.INTERRUPTED
        } catch (ex: InterruptedException) {
            logDebug("[download] Interrupted - partial file saved for resume\n$ex")
            return DownloadResult.INTERRUPTED
        } catch (ex: Throwable) {
            logDebug("[download] Exception while application downloading\n$ex")
            errorCallback(ex)
            return DownloadResult.ERROR
        } finally {
            connection?.disconnect()
            input.safeClose()
            output.safeClose()
        }
    }

    // Follows redirects manually so proxy, cookies and Range are re-applied per hop
    private fun openConnection(
        url: String,
        downloadedBytes: Long,
        proxy: Proxy?,
    ): HttpURLConnection {
        var currentUrl = url
        var redirects = 0
        while (true) {
            val httpUrl = currentUrl.toHttpUrlOrNull()
                ?: throw IllegalArgumentException("Invalid download URL")
            val connection = if (proxy != null) {
                URL(currentUrl).openConnection(proxy) as HttpURLConnection
            } else {
                URL(currentUrl).openConnection() as HttpURLConnection
            }

            val cookies = cookieJar.loadForRequest(httpUrl)
                .map { it.toString() }
                .takeIf { it.isNotEmpty() }
                ?.reduce { acc, cookie -> "$acc;$cookie" }

            with(connection) {
                setRequestProperty("Cookie", cookies)
                // Same UA as all API calls, so the server can tell official
                // client versions apart (e.g. redirect-capable ones) instead
                // of the platform default Dalvik/... string
                setRequestProperty("User-Agent", userAgentProvider.getUserAgent())
                connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
                // Without it a stalled socket blocks in read() forever and burns
                // the foreground service time budget until Android kills the app
                readTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
                requestMethod = GET
                useCaches = false
                doInput = true
                instanceFollowRedirects = false
                if (downloadedBytes > 0) {
                    setRequestProperty("Range", "bytes=$downloadedBytes-")
                }
            }
            connection.connect()

            val code = connection.responseCode
            if (code !in REDIRECT_CODES) {
                return connection
            }
            val location = connection.getHeaderField("Location")
            connection.disconnect()
            if (++redirects > MAX_REDIRECTS) {
                throw IOException("Too many redirects while downloading")
            }
            if (location == null) {
                throw IOException("Redirect $code without Location header")
            }
            currentUrl = httpUrl.resolve(location)?.toString()
                ?: throw IOException("Invalid redirect location: $location")
        }
    }

    /**
     * Entries the server has no checksum for are taken as they came — there is
     * nothing to compare against, and failing them would block those downloads
     * outright.
     */
    private fun matchesChecksum(fileName: String, sha1: String?): Boolean {
        if (sha1.isNullOrBlank()) {
            return true
        }
        // sha1() consumes and closes the stream
        val actual = apkStorage.openReadTmp(fileName)?.sha1()?.lowercase() ?: return false
        // Folded rather than compared case-insensitively: neither side is
        // promised to arrive in a particular case
        val expected = sha1.lowercase()
        if (actual != expected) {
            logDebug("[download] Checksum mismatch: expected $expected, got $actual")
            return false
        }
        return true
    }

}

const val GET = "GET"
const val SC_BAD_REQUEST = 400
const val SC_PARTIAL_CONTENT = 206
const val SC_RANGE_NOT_SATISFIABLE = 416

private val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)
private const val MAX_REDIRECTS = 5

private const val BUFFER_SIZE = 1 * 1024 * 1024

enum class DownloadResult {
    SUCCESS,
    INTERRUPTED,
    ERROR
}
