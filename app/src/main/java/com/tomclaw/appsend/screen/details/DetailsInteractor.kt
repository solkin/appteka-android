package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.api.CreateTopicResponse
import com.tomclaw.appsend.screen.details.api.DeletionResponse
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.MarkFavoriteResponse
import com.tomclaw.appsend.screen.details.api.ModerationDecisionResponse
import com.tomclaw.appsend.screen.details.api.RequestScanResponse
import com.tomclaw.appsend.screen.details.api.TranslationResponse
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Locale

interface DetailsInteractor {

    fun loadDetails(appId: String?, packageName: String?): Observable<Details>

    fun sendModerationDecision(
        appId: String,
        isApprove: Boolean
    ): Single<ModerationDecisionResponse>

    fun deleteApplication(appId: String): Single<DeletionResponse>

    fun createTopic(packageName: String): Single<CreateTopicResponse>

    fun markFavorite(appId: String, isFavorite: Boolean): Single<MarkFavoriteResponse>

    fun getUserBrief(): Single<UserBrief>

    fun translate(appId: String): Single<TranslationResponse>

    fun requestSecurityScan(appId: String): Single<RequestScanResponse>

}

class DetailsInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
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

    override fun getUserBrief(): Single<UserBrief> {
        return api
            .getUserBrief(userId = null)
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun translate(appId: String): Single<TranslationResponse> {
        return api
            .getInfoTranslation(appId, locale.language)
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun requestSecurityScan(appId: String): Single<RequestScanResponse> {
        return api
            .requestSecurityScan(appId)
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

}

const val MODERATION_APPROVE = 1
const val MODERATION_DENY = -1
