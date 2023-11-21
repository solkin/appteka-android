package com.tomclaw.appsend.user

import com.google.gson.Gson
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

interface SessionStorage {

    fun loadSessionCredentials(): Single<SessionCredentials>

    fun deleteSessionCredentials(): Single<Unit>

}

class SessionStorageImpl(
    private val dir: File,
    private val gson: Gson,
    private val schedulers: SchedulersFactory
) : SessionStorage {

    override fun loadSessionCredentials(): Single<SessionCredentials> {
        return Single
            .create<SessionCredentials> { emitter ->
                val file = File(dir, USER_DATA_FILE_NAME)
                if (!file.exists()) {
                    emitter.onError(FileNotFoundException())
                    return@create
                }
                val reader = FileReader(file)
                val credentials = gson.fromJson(reader, SessionCredentials::class.java)
                emitter.onSuccess(credentials)
            }
            .subscribeOn(schedulers.io())
    }

    override fun deleteSessionCredentials(): Single<Unit> {
        return Single
            .create { emitter ->
                val file = File(dir, USER_DATA_FILE_NAME)
                if (file.exists() && !file.delete()) {
                    emitter.onError(FileNotFoundException())
                    return@create
                }
                Appteka.app().session.userHolder.reset()
                emitter.onSuccess(Unit)
            }
            .subscribeOn(schedulers.io())
    }

}

const val USER_DATA_FILE_NAME = "user.dat"
