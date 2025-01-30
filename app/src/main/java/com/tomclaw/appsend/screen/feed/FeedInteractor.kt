package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface FeedInteractor {

    fun listFeed(
        userId: Int?,
        postId: Int? = null,
        direction: FeedDirection?,
    ): Observable<List<PostEntity>>

}

class FeedInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : FeedInteractor {

    override fun listFeed(
        userId: Int?,
        postId: Int?,
        direction: FeedDirection?,
    ): Observable<List<PostEntity>> {
        return api
            .getFeedList(userId, postId, direction?.value)
            .map { list ->
                list.result.posts
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}

enum class FeedDirection(val value: String) {
    Before("before"),
    After("after"),
    Both("both"),
}
