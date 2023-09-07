package com.tomclaw.appsend.screen.auth.request_code

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.rate.api.SubmitReviewResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface RequestCodeInteractor {

    fun submitReview(appId: String, rating: Float, review: String): Observable<SubmitReviewResponse>

}

class RequestCodeInteractorImpl(
    private val api: StoreApi,
    private val userDataInteractor: UserDataInteractor,
    private val schedulers: SchedulersFactory
) : RequestCodeInteractor {

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
