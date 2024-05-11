package com.tomclaw.appsend.screen.ratings

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.screen.ratings.api.DeleteRatingResponse
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

data class UserBriefWrapper(
    val userBrief: UserBrief?
)

interface RatingsInteractor {

    fun listRatings(offsetRateId: Int?): Observable<List<RatingEntity>>

    fun deleteRating(rateId: Int): Observable<DeleteRatingResponse>

    fun getUserBrief(): Observable<UserBriefWrapper>

}

class RatingsInteractorImpl(
    private val api: StoreApi,
    private val appId: String,
    private val schedulers: SchedulersFactory
) : RatingsInteractor {

    override fun listRatings(offsetRateId: Int?): Observable<List<RatingEntity>> {
        return api
            .getRatings(
                appId = appId,
                rateId = offsetRateId,
                count = null,
            )
            .map { list ->
                list.result.entries
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun deleteRating(rateId: Int): Observable<DeleteRatingResponse> {
        return api
            .deleteRating(rateId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun getUserBrief(): Observable<UserBriefWrapper> {
        return api
            .getUserBrief(userId = null)
            .map { UserBriefWrapper(it.result) }
            .onErrorReturn { UserBriefWrapper(userBrief = null) }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
