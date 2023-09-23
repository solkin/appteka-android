package com.tomclaw.appsend.screen.rate

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.rate.api.SubmitReviewResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface RateInteractor {

    fun submitReview(appId: String, rating: Float, review: String): Observable<SubmitReviewResponse>

}

class RateInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : RateInteractor {

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
