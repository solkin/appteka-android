package com.tomclaw.appsend.upload

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jakewharton.rxrelay3.BehaviorRelay
import com.tomclaw.appsend.core.HOST_URL
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.util.MultipartStream
import com.tomclaw.appsend.util.MultipartStream.ProgressHandler
import com.tomclaw.appsend.util.PackageHelper
import com.tomclaw.appsend.util.decodeSampledBitmapFromStream
import com.tomclaw.appsend.util.getLabel
import com.tomclaw.appsend.util.md5
import io.reactivex.rxjava3.core.Observable
import okhttp3.CookieJar
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.InterruptedIOException
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


interface UploadManager {

    fun status(id: String): Observable<UploadState>

    fun upload(id: String, pkg: UploadPackage, apk: UploadApk?, info: UploadInfo)

    fun cancel(id: String)

}

class UploadManagerImpl(
    private val context: Context,
    private val cookieJar: CookieJar,
    private val api: StoreApi,
    private val gson: Gson
) : UploadManager {

    private val executor = Executors.newSingleThreadExecutor()

    private val relays = HashMap<String, BehaviorRelay<UploadState>>()
    private val uploads = HashMap<String, Future<*>>()
    private val results = HashMap<String, UploadResponse>()

    override fun status(id: String): Observable<UploadState> {
        val relay = relays[id] ?: let {
            println("[upload] New status relay for $id")
            val relay = BehaviorRelay.createDefault(UploadState(status = UploadStatus.IDLE))
            relay.accept(UploadState(status = UploadStatus.IDLE))
            relays[id] = relay
            relay
        }
        return relay.doFinally {
            if (relay.hasObservers()) {
                println("[upload] Relay $id has observers")
                return@doFinally
            }
            val inactiveState = relay.hasValue() &&
                    (relay.value?.status == UploadStatus.IDLE
                            || relay.value?.status == UploadStatus.COMPLETED
                            || relay.value?.status == UploadStatus.ERROR)
            println("[upload] Relay $id is inactive: $inactiveState")
            if (!relay.hasValue() || inactiveState) {
                relays.remove(id)
                results.remove(id)
                println("[upload] Relay $id removed")
            }
        }
    }

    override fun upload(id: String, pkg: UploadPackage, apk: UploadApk?, info: UploadInfo) {
        val relay = relays[id] ?: BehaviorRelay.create()
        if (info.checkExist.file != null && !results.containsKey(id)) {
            val file = info.checkExist.file
            results[id] = UploadResponse(
                appId = file.appId,
                fileStatus = file.status,
            )
        }
        relay.accept(UploadState(status = UploadStatus.AWAIT))
        uploads[id] = executor.submit {
            val scrUploadUri = info.screenshots
                .filter { it.scrId.isNullOrEmpty() }
                .map { it.original }
            val scrUploadCount = scrUploadUri.size
            var apkCount = 0

            relay.accept(UploadState(status = UploadStatus.STARTED))
            val uploadResult = results[id] ?: if (apk != null) {
                apkCount = 1
                uploadBlocking(
                    path = apk.path,
                    packageInfo = apk.packageInfo,
                    progressCallback = { percent ->
                        relay.accept(
                            UploadState(
                                status = UploadStatus.PROGRESS,
                                totalPercent(apkCount, percent, scrUploadCount, 0)
                            )
                        )
                    },
                    errorCallback = {
                        relay.accept(UploadState(status = UploadStatus.ERROR))
                    },
                    cancelCallback = {},
                )
            } else {
                null
            }
            if (uploadResult != null) {
                results[id] = uploadResult

                val scrIds = scrUploadUri
                    .takeIf { it.isNotEmpty() }
                    ?.let { uris ->
                        uploadScreenshotsBlocking(uris, progressCallback = { percent ->
                            relay.accept(
                                UploadState(
                                    status = UploadStatus.PROGRESS,
                                    totalPercent(apkCount, 100, scrUploadCount, percent)
                                )
                            )
                        })
                    }
                    .orEmpty()
                    .let { ids -> mergeEmptyStrings(info.screenshots.map { it.scrId }, ids) }


                setMetaInfoBlocking(
                    appId = uploadResult.appId,
                    info = info,
                    scrIds = scrIds,
                    successCallback = {
                        relay.accept(
                            UploadState(
                                status = UploadStatus.COMPLETED,
                                result = uploadResult
                            )
                        )
                    },
                    errorCallback = {
                        relay.accept(UploadState(status = UploadStatus.ERROR))
                    },
                )
            }
            uploads.remove(id)
        }
        relays[id] = relay
        return
    }

    override fun cancel(id: String) {
        uploads.remove(id)?.cancel(true)
        relays[id]?.accept(UploadState(status = UploadStatus.IDLE))
    }

    @SuppressLint("DefaultLocale")
    private fun uploadScreenshotsBlocking(
        uris: List<Uri>,
        progressCallback: (Int) -> (Unit),
    ): List<String>? {
        var connection: HttpURLConnection? = null
        try {
            val boundary = generateBoundary()

            connection = openMultipartConnection(HOST_UPLOAD_SCREENSHOT_URL, boundary)

            connection.outputStream.use { outputStream ->
                val multipart = MultipartStream(outputStream, boundary)
                uris.forEachIndexed { index, uri ->
                    val name = String.format(
                        format = "src%d.jpg",
                        uri.hashCode()
                    )
                    multipart.writePart(
                        "images",
                        name,
                        writer = { outputStream ->
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                val bitmap = decodeSampledBitmapFromStream(
                                    inputStream,
                                    SCREENSHOT_MAX_PIXELS
                                ) ?: throw IOException("unable to decode file")
                                bitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    SCREENSHOT_JPEG_QUALITY,
                                    outputStream
                                )
                                outputStream.flush()
                            } ?: throw IOException("unable to read file")
                        },
                        "image/jpeg",
                        object : ProgressHandler {
                            override fun onProgress(sent: Long) {}
                            override fun onError(ex: Throwable) {}
                            override fun onCancelled(ex: Throwable) {
                                throw ex
                            }
                        }
                    )
                    progressCallback((index + 1) * 100 / uris.size)
                }
                multipart.writeLastBoundaryIfNeeds()
                multipart.flush()
            }

            when (val responseCode = connection.responseCode) {
                200 -> {
                    return InputStreamReader(connection.inputStream).use { reader ->
                        val responseType: Type =
                            object : TypeToken<StoreResponse<UploadScreenshotsResponse>>() {}.type
                        val response: StoreResponse<UploadScreenshotsResponse> =
                            gson.fromJson(reader, responseType)
                        response.result.scrIds
                    }
                }

                else -> {
                    InputStreamReader(connection.errorStream).use { reader ->
                        println(reader.readText())
                    }
                    throw IOException("Error upload response code is $responseCode")
                }
            }
        } catch (ex: InterruptedIOException) {
            println("[upload] IO interruption while application uploading\n$ex")
        } catch (ex: InterruptedException) {
            println("[upload] Interruption while application uploading\n$ex")
        } catch (ex: Throwable) {
            println("[upload] Exception while application uploading\n$ex")
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun setMetaInfoBlocking(
        appId: String,
        info: UploadInfo,
        scrIds: List<String>,
        successCallback: (SetMetaResponse) -> Unit,
        errorCallback: (Throwable) -> Unit,
    ) {
        return api.setMeta(
            appId,
            category = info.category.id,
            description = info.description,
            whatsNew = info.whatsNew,
            exclusive = info.exclusive,
            sourceUrl = info.sourceUrl,
            scrIds = scrIds,
            private = false,
        ).blockingSubscribe(
            { successCallback.invoke(it.result) },
            { errorCallback.invoke(it) }
        )
    }

    private fun uploadBlocking(
        path: String,
        packageInfo: PackageInfo,
        progressCallback: (Int) -> Unit,
        errorCallback: (Throwable) -> Unit,
        cancelCallback: () -> Unit
    ): UploadResponse? {
        val packageManager = context.packageManager
        val apk = File(path)
        val label = packageInfo.getLabel()
        val icon = PackageHelper.getPackageIconPng(
            packageInfo.applicationInfo,
            packageManager
        )
        val size = apk.length() + icon.size
        val apkName = packageInfo.packageName.md5() + ".apk"
        val iconName = packageInfo.packageName.md5() + ".png"

        var connection: HttpURLConnection? = null
        try {
            val boundary = generateBoundary()

            connection = openMultipartConnection(HOST_UPLOAD_APP_URL, boundary)

            connection.outputStream.use { outputStream ->
                val multipart = MultipartStream(outputStream, boundary)
                multipart.writePart("label", label)
                ByteArrayInputStream(icon).use { iconStream ->
                    multipart.writePart(
                        "icon_file",
                        iconName,
                        iconStream,
                        "image/png",
                        object : ProgressHandler {
                            override fun onProgress(sent: Long) {}
                            override fun onError(ex: Throwable) {}
                            override fun onCancelled(ex: Throwable) {
                                throw ex
                            }
                        }
                    )
                }
                FileInputStream(apk).use { apkStream ->
                    multipart.writePart(
                        "apk_file",
                        apkName,
                        apkStream,
                        "application/vnd.android.package-archive",
                        object : ProgressHandler {
                            override fun onProgress(sent: Long) {
                                val percent = if (size > 0) (100 * sent / size).toInt() else 0
                                progressCallback(percent)
                            }

                            override fun onError(ex: Throwable) {
                                errorCallback.invoke(ex)
                            }

                            override fun onCancelled(ex: Throwable) {
                                throw ex
                            }
                        }
                    )
                }
                multipart.writeLastBoundaryIfNeeds()
                multipart.flush()
            }

            when (val responseCode = connection.responseCode) {
                200 -> {
                    return InputStreamReader(connection.inputStream).use { reader ->
                        val responseType: Type =
                            object : TypeToken<StoreResponse<UploadResponse>>() {}.type
                        val response: StoreResponse<UploadResponse> =
                            gson.fromJson(reader, responseType)
                        response.result
                    }
                }

                else -> {
                    InputStreamReader(connection.errorStream).use { reader ->
                        println(reader.readText())
                    }
                    throw IOException("Error upload response code is $responseCode")
                }
            }
        } catch (ex: InterruptedIOException) {
            println("[upload] IO interruption while application uploading\n$ex")
            cancelCallback.invoke()
        } catch (ex: InterruptedException) {
            println("[upload] Interruption while application uploading\n$ex")
            cancelCallback.invoke()
        } catch (ex: Throwable) {
            println("[upload] Exception while application uploading\n$ex")
            errorCallback.invoke(ex)
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun openMultipartConnection(url: String, boundary: String): HttpURLConnection {
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection

        val httpUrl = url.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid upload screenshot URL")

        val cookies = cookieJar.loadForRequest(httpUrl)
            .map { it.toString() }
            .takeIf { it.isNotEmpty() }
            ?.reduce { acc, cookie -> "$acc;$cookie" }

        with(connection) {
            setRequestProperty("Content-Type", "multipart/form-data;boundary=$boundary")
            setRequestProperty("Cookie", cookies)
            readTimeout = TimeUnit.MINUTES.toMillis(2).toInt()
            connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
            requestMethod = POST
            useCaches = false
            doInput = true
            doOutput = true
            instanceFollowRedirects = false
            setChunkedStreamingMode(128000)
            connect()
        }
        return connection
    }

    private fun generateBoundary(): String = UUID.randomUUID().toString().filter { it == '-' }

}

fun mergeEmptyStrings(left: List<String?>, right: List<String>?): List<String> {
    val iterator = right.orEmpty().iterator()
    return left
        .map { v ->
            v ?: if (iterator.hasNext()) {
                iterator.next()
            } else {
                ""
            }
        }
        .filter { it.isNotEmpty() }
}

fun totalPercent(
    apkCount: Int,
    apkPercent: Int,
    scrCount: Int,
    scrPercent: Int,
    apkWeight: Int = 80,
    scrWeight: Int = 20
): Int {
    val apk = if (scrCount > 0) apkPercent * apkWeight / 100 else apkPercent
    val scr = if (apkCount > 0) scrPercent * scrWeight / 100 else scrPercent
    return (if (apkCount > 0) apk else 0) + (if (scrCount > 0) scr else 0)
}

const val POST = "POST"
const val SCREENSHOT_MAX_PIXELS = 2000000
const val SCREENSHOT_JPEG_QUALITY = 90
const val HOST_UPLOAD_APP_URL = "$HOST_URL/api/1/app/upload"
const val HOST_UPLOAD_SCREENSHOT_URL = "$HOST_URL/api/1/screenshot/upload"
