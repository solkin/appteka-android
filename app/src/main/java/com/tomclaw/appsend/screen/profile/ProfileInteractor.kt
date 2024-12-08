package com.tomclaw.appsend.screen.profile

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.profile.api.EliminateUserResponse
import com.tomclaw.appsend.screen.profile.api.Profile
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.screen.profile.api.SetUserNameResponse
import com.tomclaw.appsend.screen.profile.api.SubscribeResponse
import com.tomclaw.appsend.screen.profile.api.UnsubscribeResponse
import com.tomclaw.appsend.screen.profile.api.UserAppsResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

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
        return Single.create<ProfileResponse> { emitter ->
            emitter.onSuccess(
                ProfileResponse(
                    profile = Profile(
                        userId = 1,
                        name = "Admin",
                        nameRegex = null,
                        userIcon = UserIcon("<svg viewBox=\"0 0 72 72\" xmlns=\"http://www.w3.org/2000/svg\"><path fill=\"#9B9B9A\" d=\"M39.11 21.888l7.778-7.778 11 11-7.778 7.778z\"/><path fill=\"#3F3F3F\" d=\"M46.034 22.212l4.478-4.478 7.377 7.377-5.367 5.367z\"/><circle cx=\"31.769\" cy=\"40.404\" r=\"23\" fill=\"#9B9B9A\"/><path fill=\"#3F3F3F\" d=\"M19.633 55.737c12.703 0 23-10.297 23-23a22.904 22.904 0 00-5.21-14.576C47.286 20.754 54.56 29.73 54.56 40.404c0 12.702-10.297 23-23 23-7.17 0-13.572-3.282-17.79-8.424 1.873.492 3.837.757 5.864.757z\"/><g fill=\"none\" stroke=\"#000\" stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-miterlimit=\"10\" stroke-width=\"2\"><path d=\"M41.46 19.54l5.429-5.43 11 11-5.367 5.367\"/><circle cx=\"31.769\" cy=\"40.404\" r=\"23\"/><path d=\"M55.757 16.243l8.486-8.486\"/></g></svg>", mapOf("en" to "Bomb"), "#DA5100"),
                        joinTime = 0,
                        lastSeen = 0,
                        role = 100,
                        mentorId = 0,
                        filesCount = 0,
                        favoritesCount = 0,
                        totalDownloads = 0,
                        msgCount = 0,
                        reviewsCount = 0,
                        feedCount = 1,
                        pubsCount = 0,
                        subsCount = 0,
                        lastReviews = null,
                        isRegistered = false,
                        isVerified = true,
                        isSubscribed = false,
                        url = ""
                    ),
                    grantRoles = emptyList()
                )
            )
        }
        /*return api
            .getProfile(userId)
            .map { it.result }*/
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun loadUserApps(userId: Int?, offsetAppId: String?): Observable<UserAppsResponse> {
        return Single.create<UserAppsResponse> { emitter ->
            emitter.onSuccess(UserAppsResponse(
                files = emptyList()
            ))
        }
        /*return api
            .getUserApps(
                userId = userId,
                appId = offsetAppId,
            )
            .map { list ->
                list.result
            }*/
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
