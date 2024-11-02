package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface FeedInteractor {

    fun listFeed(userId: Int?, postId: Int? = null): Observable<List<PostEntity>>

}

class FeedInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : FeedInteractor {

    override fun listFeed(userId: Int?, postId: Int?): Observable<List<PostEntity>> {
        return api
            .getFeedList(userId, postId)
            .map { list ->
                list.result.posts
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
