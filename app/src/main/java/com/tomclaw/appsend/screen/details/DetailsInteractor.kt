package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.details.api.CreateTopicResponse
import com.tomclaw.appsend.screen.details.api.DeletionResponse
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.MarkFavoriteResponse
import com.tomclaw.appsend.screen.details.api.AIReview
import com.tomclaw.appsend.screen.details.api.ModerationDecisionResponse
import com.tomclaw.appsend.screen.details.api.RejectionReason
import com.tomclaw.appsend.screen.details.api.RejectionReasonsResponse
import com.tomclaw.appsend.screen.details.api.RequestAIReviewResponse
import com.tomclaw.appsend.screen.details.api.RequestScanResponse
import com.tomclaw.appsend.screen.details.api.TranslationResponse
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.bananalytics.Bananalytics
import com.tomclaw.bananalytics.api.BreadcrumbCategory
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Locale

interface DetailsInteractor {

    fun loadDetails(appId: String?, packageName: String?): Observable<Details>

    fun sendModerationDecision(
        appId: String,
        isApprove: Boolean,
        reasonCode: Int? = null,
        reasonComment: String? = null,
    ): Single<ModerationDecisionResponse>

    fun loadRejectionReasons(): Single<List<RejectionReason>>

    fun loadAIReview(appId: String): Maybe<AIReview>

    fun applyAIReview(appId: String): Single<ModerationDecisionResponse>

    fun deleteApplication(appId: String): Single<DeletionResponse>

    fun createTopic(packageName: String): Single<CreateTopicResponse>

    fun markFavorite(appId: String, isFavorite: Boolean): Single<MarkFavoriteResponse>

    fun getUserBrief(): Single<UserBrief>

    fun translate(appId: String): Single<TranslationResponse>

    fun requestSecurityScan(appId: String): Single<RequestScanResponse>

    fun requestAIReview(appId: String): Single<RequestAIReviewResponse>

}

class DetailsInteractorImpl(
    private val bananalytics: Bananalytics,
    private val api: StoreApi,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : DetailsInteractor {

    override fun loadDetails(appId: String?, packageName: String?): Observable<Details> {
        bananalytics.leaveBreadcrumb("Load details: $appId", BreadcrumbCategory.NETWORK)
        return api
            .getInfo(
                appId = appId,
                packageName = packageName,
                locale = locale.language,
            )
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun sendModerationDecision(
        appId: String,
        isApprove: Boolean,
        reasonCode: Int?,
        reasonComment: String?,
    ): Single<ModerationDecisionResponse> {
        val decision = if (isApprove) MODERATION_APPROVE else MODERATION_DENY
        return api
            .sendModerationDecision(
                appId = appId,
                decision = decision,
                reasonCode = reasonCode,
                reasonComment = reasonComment?.takeIf { it.isNotBlank() },
            )
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun loadRejectionReasons(): Single<List<RejectionReason>> {
        return api
            .getRejectionReasons(locale = locale.language)
            .map { it.result.reasons }
            .subscribeOn(schedulers.io())
    }

    override fun loadAIReview(appId: String): Maybe<AIReview> {
        return api
            .getAIReview(appId = appId, locale = locale.language)
            .filter { it.result.review != null }
            .map { it.result.review!! }
            .subscribeOn(schedulers.io())
    }

    override fun applyAIReview(appId: String): Single<ModerationDecisionResponse> {
        return api
            .applyAIReview(appId = appId)
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun deleteApplication(appId: String): Single<DeletionResponse> {
        bananalytics.leaveBreadcrumb("Delete app: $appId", BreadcrumbCategory.NETWORK)
        return api.deleteApplication(appId = appId)
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

    override fun createTopic(packageName: String): Single<CreateTopicResponse> {
        val textPlain = "text/plain".toMediaTypeOrNull()
        return api.createTopic(
            packageName = packageName.toRequestBody(textPlain),
            title = null,
            description = null,
            avatar = null,
        )
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

    override fun requestAIReview(appId: String): Single<RequestAIReviewResponse> {
        return api
            .requestAIReview(appId)
            .map { it.result }
            .subscribeOn(schedulers.io())
    }

}

const val MODERATION_APPROVE = 1
const val MODERATION_DENY = -1
