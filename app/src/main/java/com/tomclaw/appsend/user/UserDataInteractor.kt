package com.tomclaw.appsend.user

import com.google.gson.Gson
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.UserData
import io.reactivex.rxjava3.core.Single
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

interface UserDataInteractor {

    fun getUserData(): Single<UserData>

}

class UserDataInteractorImpl(
    private val dir: File,
    private val gson: Gson,
    private val api: StoreApi
) : UserDataInteractor {

    private var userData: UserData? = null

    override fun getUserData(): Single<UserData> {
        return getCachedUserData() ?: loadSessionCredentials()
            .flatMap { loadUserData(it.guid) }
            .map { cacheUserData(it) }
    }

    private fun getCachedUserData(): Single<UserData>? {
        return userData?.let { Single.just(it) }
    }

    private fun cacheUserData(userData: UserData): UserData {
        this.userData = userData
        return userData
    }

    private fun loadSessionCredentials(): Single<SessionCredentials> {
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

    private fun loadUserData(guid: String): Single<UserData> {
        return api
            .getUserData(guid)
            .map {
                with(it.result.profile) {
                    UserData(
                        guid = guid,
                        userId = userId,
                        userIcon = userIcon,
                        role = role,
                        email = email,
                        name = name,
                    )
                }
            }
    }

}

const val USER_DATA_FILE_NAME = "user.dat"
