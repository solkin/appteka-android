package com.tomclaw.appsend.screen.rate

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.rate.api.SubmitReviewResponse
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface RateInteractor {

    fun getUserBrief(): Observable<UserBrief>

    fun submitReview(appId: String, rating: Float, review: String): Observable<SubmitReviewResponse>

}

class RateInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : RateInteractor {

    override fun getUserBrief(): Observable<UserBrief> {
        return api
            .getUserBrief(userId = null)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun submitReview(
        appId: String,
        rating: Float,
        review: String
    ): Observable<SubmitReviewResponse> {
        return api
            .submitReview(
                appId = appId,
                score = rating.toInt(),
                text = review,
            )
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
