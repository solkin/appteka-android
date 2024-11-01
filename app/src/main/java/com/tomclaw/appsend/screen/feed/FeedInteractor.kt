package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.feed.api.FeedEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface FeedInteractor {

    fun listFeed(userId: Int, postId: Int? = null): Observable<List<FeedEntity>>

}

class FeedInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : FeedInteractor {

    override fun listFeed(userId: Int, postId: Int?): Observable<List<FeedEntity>> {
        return api
            .getFeedList(userId, postId)
            .map { list ->
                list.result.entries
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
