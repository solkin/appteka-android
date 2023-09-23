package com.tomclaw.appsend.upload

import android.content.pm.PackageInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jakewharton.rxrelay3.BehaviorRelay
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.core.Config
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.HttpUtil
import com.tomclaw.appsend.util.MultipartStream
import com.tomclaw.appsend.util.MultipartStream.ProgressHandler
import com.tomclaw.appsend.util.PackageHelper
import com.tomclaw.appsend.util.getLabel
import com.tomclaw.appsend.util.md5
import io.reactivex.rxjava3.core.Observable
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
    private val userDataInteractor: UserDataInteractor,
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
        val result = results[id]
        if (result != null) {
            setMetaInfoBlocking(result.appId, info)
            relay.accept(UploadState(status = UploadStatus.COMPLETED, result = result))
            return
        }
        relay.accept(UploadState(status = UploadStatus.AWAIT))
        uploads[id] = executor.submit {
            relay.accept(UploadState(status = UploadStatus.STARTED))
            val uploadResult = if (apk != null) {
                uploadBlocking(
                    path = apk.path,
                    packageInfo = apk.packageInfo,
                    progressCallback = { percent ->
                        relay.accept(UploadState(status = UploadStatus.PROGRESS, percent))
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
                setMetaInfoBlocking(uploadResult.appId, info)
                relay.accept(UploadState(status = UploadStatus.COMPLETED, result = uploadResult))
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

    private fun setMetaInfoBlocking(
        appId: String,
        info: UploadInfo
    ) {
        return api.setMeta(
            appId,
            category = info.category.id,
            description = info.description,
            whatsNew = info.whatsNew,
            exclusive = info.exclusive,
            sourceUrl = info.sourceUrl,
        ).blockingSubscribe(
            {}, {}
        )
    }

    private fun uploadBlocking(
        path: String,
        packageInfo: PackageInfo,
        progressCallback: (Int) -> Unit,
        errorCallback: (Throwable) -> Unit,
        cancelCallback: () -> Unit
    ): UploadResponse? {
        val packageManager = Appteka.app().packageManager
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
            val url = URL(HOST_UPLOAD_URL)
            connection = url.openConnection() as HttpURLConnection

            val boundary = generateBoundary()

            with(connection) {
                setRequestProperty(
                    "Content-Type",
                    "multipart/form-data;boundary=$boundary"
                )
                readTimeout = TimeUnit.MINUTES.toMillis(2).toInt()
                connectTimeout = TimeUnit.SECONDS.toMillis(30).toInt()
                requestMethod = HttpUtil.POST
                useCaches = false
                doInput = true
                doOutput = true
                instanceFollowRedirects = false
                setChunkedStreamingMode(128000)
                connect()
            }

            connection.outputStream.use { outputStream ->
                val multipart = MultipartStream(outputStream, boundary)
                val userData = userDataInteractor.getUserData().blockingGet()
                multipart.writePart("guid", userData.guid)
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

    private fun generateBoundary(): String = UUID.randomUUID().toString().filter { it == '-' }

}

const val HOST_UPLOAD_URL = Config.HOST_URL + "/api/1/app/upload"
