package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.api.CreateTopicResponse
import com.tomclaw.appsend.screen.details.api.DeletionResponse
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.MarkFavoriteResponse
import com.tomclaw.appsend.screen.details.api.ModerationDecisionResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface DetailsInteractor {

    fun loadDetails(appId: String?, packageName: String?): Observable<Details>

    fun sendModerationDecision(
        appId: String,
        isApprove: Boolean
    ): Single<ModerationDecisionResponse>

    fun deleteApplication(appId: String): Single<DeletionResponse>

    fun createTopic(packageName: String): Single<CreateTopicResponse>

    fun markFavorite(appId: String, isFavorite: Boolean): Single<MarkFavoriteResponse>

}

class DetailsInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : DetailsInteractor {

    override fun loadDetails(appId: String?, packageName: String?): Observable<Details> {
        return api
            .getInfo(
                appId = appId,
                packageName = packageName,
            )
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun sendModerationDecision(
        appId: String,
        isApprove: Boolean
    ): Single<ModerationDecisionResponse> {
        val decision = if (isApprove) MODERATION_APPROVE else MODERATION_DENY
        return api
            .sendModerationDecision(
                appId = appId,
                decision = decision,
            )
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun deleteApplication(appId: String): Single<DeletionResponse> {
        return api.deleteApplication(appId = appId)
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun createTopic(packageName: String): Single<CreateTopicResponse> {
        return api.createTopic(packageName = packageName)
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun markFavorite(appId: String, isFavorite: Boolean): Single<MarkFavoriteResponse> {
        return api
            .markFavorite(
                appId = appId,
                isFavorite = isFavorite,
            )
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

}

const val MODERATION_APPROVE = 1
const val MODERATION_DENY = -1
