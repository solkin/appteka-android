package com.tomclaw.appsend.screen.reviews

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.reviews.api.ReviewEntity
import com.tomclaw.appsend.screen.reviews.api.ReviewsResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface ReviewsInteractor {

    fun listReviews(offsetRateId: Int?): Observable<List<ReviewEntity>>

}

class ReviewsInteractorImpl(
    private val api: StoreApi,
    private val userId: Int,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : ReviewsInteractor {

    override fun listReviews(offsetRateId: Int?): Observable<List<ReviewEntity>> {
        return api
            .getUserReviews(
                userId = userId,
                rateId = offsetRateId,
                locale = locale.language
            )
            .map { list ->
                list.result.entries
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
