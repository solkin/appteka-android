package com.tomclaw.appsend.screen.auth.verify_code

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.rate.api.SubmitReviewResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface VerifyCodeInteractor {

    fun submitReview(appId: String, rating: Float, review: String): Observable<SubmitReviewResponse>

}

class VerifyCodeInteractorImpl(
    private val api: StoreApi,
    private val userDataInteractor: UserDataInteractor,
    private val schedulers: SchedulersFactory
) : VerifyCodeInteractor {

    override fun submitReview(
        appId: String,
        rating: Float,
        review: String
    ): Observable<SubmitReviewResponse> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.submitReview(
                    guid = it.guid,
                    appId = appId,
                    score = rating.toInt(),
                    text = review,
                )
            }
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
