package com.tomclaw.appsend.screen.upload

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.Locale

interface UploadInteractor {

    fun calculateSha1(file: String): Observable<String>

    fun checkExist(sha1: String): Observable<CheckExistResponse>

}

class UploadInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val userDataInteractor: UserDataInteractor,
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

    override fun checkExist(sha1: String): Observable<CheckExistResponse> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.checkExist(
                    guid = it.guid,
                    sha1 = sha1,
                    locale = locale.language,
                )
            }
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    private fun ByteArray.toHex(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

}