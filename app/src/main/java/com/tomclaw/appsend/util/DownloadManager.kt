package com.tomclaw.appsend.util

import android.os.Parcelable
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

interface DownloadManager {

    fun status(appId: String): Observable<DownloadState>

    fun download(appId: String, url: String)

    fun cancel(appId: String)

}

class DownloadManagerImpl(
    private val dir: File,
    private val schedulers: SchedulersFactory,
) : DownloadManager {

    private val packages = HashMap<String, BehaviorRelay<DownloadState>>()
    private val downloads = HashMap<String, Disposable>()

    override fun status(appId: String): Observable<DownloadState> {
        return packages[appId] ?: let {
            val status = DownloadState(status = Status.IDLE, percent = 0)
            val relay = BehaviorRelay.createDefault(status)
            packages[appId] = relay
            relay
        }
    }

    override fun download(appId: String, url: String) {
        val status = DownloadState(status = Status.AWAIT, percent = 0)
        val relay = packages[appId] ?: BehaviorRelay.createDefault(status)
        val file = File(dir, "$appId.apk") // TODO: make file name human-readable
        val disposable = downloadInternal(url, file)
            .doOnComplete {
                relay.accept(DownloadState(status = Status.COMPLETED, percent = 100))
            }
            .subscribeOn(schedulers.io())
            .subscribe(
                { percent ->
                    relay.accept(DownloadState(status = Status.PROGRESS, percent = percent))
                }, {
                    relay.accept(DownloadState(status = Status.ERROR, percent = 0))
                }
            )
        downloads[appId] = disposable
        packages[appId] = relay
    }

    override fun cancel(appId: String) {
        downloads.remove(appId)?.dispose()
        packages.remove(appId)
    }

    private fun downloadInternal(url: String, file: File): Observable<Int> {
        return Observable.create { emitter ->
            var connection: HttpURLConnection? = null
            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                LegacyLogger.log(String.format("Download app url: %s", url))
                val u = URL(url)
                connection = u.openConnection() as HttpURLConnection
                // Executing request.
                with(connection) {
                    connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
                    requestMethod = HttpUtil.GET
                    useCaches = false
                    doInput = true
                    doOutput = true
                    instanceFollowRedirects = false
                    connect()
                }
                // Open connection to response.
                val responseCode = connection.responseCode
                // Checking for this is error stream.
                input = if (responseCode >= HttpUtil.SC_BAD_REQUEST) {
                    connection.errorStream
                } else {
                    connection.inputStream
                }
                val total = connection.contentLength
                if (total <= 0) {
                    emitter.onError(IOException("ContentLength is not defined"))
                    return@create
                }
                file.parentFile?.mkdirs()
                if (file.exists()) {
                    file.delete()
                }
                output = FileOutputStream(file)
                val buffer = VariableBuffer()
                var cache: Int
                var read: Long = 0
                var percent: Int
                buffer.onExecuteStart()
                while (input.read(buffer.calculateBuffer()).also { cache = it } != -1) {
                    buffer.onExecuteCompleted(cache)
                    output.write(buffer.buffer, 0, cache)
                    output.flush()
                    read += cache.toLong()
                    percent = (100 * read / total).toInt()
                    emitter.onNext(percent)
                    buffer.onExecuteStart()
                }
                emitter.onComplete()
            } catch (ex: Throwable) {
                LegacyLogger.log("Exception while application downloading", ex)
                emitter.onError(ex)
            } finally {
                // Trying to disconnect in any case.
                connection?.disconnect()
                HttpUtil.closeSafely(input)
                HttpUtil.closeSafely(output)
            }
        }
    }

}

@Parcelize
data class DownloadState(
    val status: Status,
    val percent: Int
) : Parcelable

enum class Status {
    IDLE, AWAIT, PROGRESS, COMPLETED, ERROR
}
