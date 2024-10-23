package com.tomclaw.appsend.screen.profile

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.profile.api.EliminateUserResponse
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.screen.profile.api.SetUserNameResponse
import com.tomclaw.appsend.screen.profile.api.SubscribeResponse
import com.tomclaw.appsend.screen.profile.api.UnsubscribeResponse
import com.tomclaw.appsend.screen.profile.api.UserAppsResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface ProfileInteractor {

    fun loadProfile(userId: Int?): Observable<ProfileResponse>

    fun loadUserApps(userId: Int?, offsetAppId: String?): Observable<UserAppsResponse>

    fun setUserName(name: String): Observable<SetUserNameResponse>

    fun subscribe(userId: Int): Observable<SubscribeResponse>

    fun unsubscribe(userId: Int): Observable<UnsubscribeResponse>

    fun eliminateUser(userId: Int): Observable<EliminateUserResponse>

}

class ProfileInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : ProfileInteractor {

    override fun loadProfile(userId: Int?): Observable<ProfileResponse> {
        return api
            .getProfile(userId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun loadUserApps(userId: Int?, offsetAppId: String?): Observable<UserAppsResponse> {
        return api
            .getUserApps(
                userId = userId,
                appId = offsetAppId,
            )
            .map { list ->
                list.result
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun setUserName(name: String): Observable<SetUserNameResponse> {
        return api
            .setUserName(name)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun subscribe(userId: Int): Observable<SubscribeResponse> {
        return api
            .subscribe(userId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun unsubscribe(userId: Int): Observable<UnsubscribeResponse> {
        return api
            .unsubscribe(userId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun eliminateUser(userId: Int): Observable<EliminateUserResponse> {
        return api
            .eliminateUser(userId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
