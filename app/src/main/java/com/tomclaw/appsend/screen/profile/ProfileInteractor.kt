package com.tomclaw.appsend.screen.profile

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface ProfileInteractor {

    fun loadProfile(userId: Int): Observable<ProfileResponse>

}

class ProfileInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : ProfileInteractor {

    override fun loadProfile(userId: Int): Observable<ProfileResponse> {
        return api.getProfile(userId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
