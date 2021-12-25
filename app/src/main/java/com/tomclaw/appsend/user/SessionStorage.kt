package com.tomclaw.appsend.user

import com.google.gson.Gson
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

interface SessionStorage {

    fun loadSessionCredentials(): Single<SessionCredentials>

}

class SessionStorageImpl(
    private val dir: File,
    private val gson: Gson,
) : SessionStorage {

    override fun loadSessionCredentials(): Single<SessionCredentials> {
        return Single.create { emitter ->
            val file = File(dir, USER_DATA_FILE_NAME)
            if (!file.exists()) {
                emitter.onError(FileNotFoundException())
                return@create
            }
            val reader = FileReader(file)
            val credentials = gson.fromJson(reader, SessionCredentials::class.java)
            emitter.onSuccess(credentials)
        }
    }

}

const val USER_DATA_FILE_NAME = "user.dat"
