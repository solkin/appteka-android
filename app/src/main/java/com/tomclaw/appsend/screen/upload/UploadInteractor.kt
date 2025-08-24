package com.tomclaw.appsend.screen.upload

import android.net.Uri
import androidx.core.net.toFile
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.StreamsProvider
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale

interface UploadInteractor {

    fun calculateSha1(file: String): Observable<String>

    fun checkExist(sha1: String, packageName: String, size: Long): Observable<CheckExistResponse>

    fun uriToFile(uri: Uri): Observable<File>

}

class UploadInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val appsDir: File,
    private val streamsProvider: StreamsProvider,
    private val schedulers: SchedulersFactory
) : UploadInteractor {

    private val buffer = ByteArray(65536)

    override fun calculateSha1(file: String): Observable<String> {
        return Single
            .create { emitter ->
                val digest = MessageDigest.getInstance("SHA-1")
                val stream = BufferedInputStream(FileInputStream(file))
                stream.use { input ->
                    var n = 0
                    while (n != -1) {
                        n = input.read(buffer)
                        if (n > 0) {
                            digest.update(buffer, 0, n)
                        }
                    }
                }
                val result = digest.digest().toHex()
                emitter.onSuccess(result)
            }
            .subscribeOn(schedulers.io())
            .toObservable()
    }

    override fun checkExist(sha1: String, packageName: String, size: Long): Observable<CheckExistResponse> {
        return api
            .checkExist(
                sha1 = sha1,
                packageName = packageName,
                size = size,
                locale = locale.language,
            )
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun uriToFile(uri: Uri): Observable<File> {
        return Single
            .create { emitter ->
                if (uri.isFile()) {
                    emitter.onSuccess(uri.toFile())
                    return@create
                }
                try {
                    val temp = File.createTempFile("pick", "upload", appsDir)
                    temp.deleteOnExit()
                    val target = Uri.fromFile(temp)
                    streamsProvider.openInputStream(uri)?.let { input ->
                        streamsProvider.openOutputStream(target)?.let { output ->
                            input.copyTo(output)
                            output.flush()
                            emitter.onSuccess(temp)
                        } ?: emitter.onError(Throwable("Output stream opening error"))
                    } ?: emitter.onError(Throwable("Input stream opening error"))
                } catch (ex: Throwable) {
                    emitter.onError(ex)
                }
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

}

private fun Uri.isFile(): Boolean {
    return scheme == "file"
}
