package com.tomclaw.appsend.screen.profile

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.profile.api.EliminateUserResponse
import com.tomclaw.appsend.screen.profile.api.SubscribeResponse
import com.tomclaw.appsend.screen.profile.api.UnsubscribeResponse
import com.tomclaw.appsend.screen.profile.api.UserAppsResponse
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.isHttpError
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface ProfileInteractor {

    fun loadProfile(userId: Int?): Observable<ProfileData>

    fun loadUserApps(userId: Int?, offsetAppId: String?): Observable<UserAppsResponse>

    fun subscribe(userId: Int): Observable<SubscribeResponse>

    fun unsubscribe(userId: Int): Observable<UnsubscribeResponse>

    fun eliminateUser(userId: Int): Observable<EliminateUserResponse>

}

class ProfileInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : ProfileInteractor {

    override fun loadProfile(userId: Int?): Observable<ProfileData> {
        val profile = api.getProfile(userId).map { it.result.profile }
        val data = if (userId == null) {
            profile.map { ProfileData(it, viewerId = it.userId) }
        } else {
            val viewerId = api.getUserBrief(userId = null)
                .map { it.result.id }
                .onErrorResumeNext { error ->
                    if (error.isHttpError(401)) Single.just(GUEST_USER_ID) else Single.error(error)
                }
            Single.zip(profile, viewerId) { target, viewer ->
                ProfileData(target, viewerId = viewer)
            }
        }
        return data.toObservable().subscribeOn(schedulers.io())
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

private const val GUEST_USER_ID = 0
