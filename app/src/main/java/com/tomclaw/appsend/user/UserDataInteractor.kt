package com.tomclaw.appsend.user

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.UserData
import io.reactivex.rxjava3.core.Single

interface UserDataInteractor {

    fun getUserData(): Single<UserData>

}

class UserDataInteractorImpl(
    private val sessionStorage: SessionStorage,
    private val api: StoreApi
) : UserDataInteractor {

    private var userData: UserData? = null

    override fun getUserData(): Single<UserData> {
        return getCachedUserData() ?: sessionStorage.loadSessionCredentials()
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
