package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.feed.api.DeletePostResponse
import com.tomclaw.appsend.screen.feed.api.FeedReactionResponse
import com.tomclaw.appsend.screen.feed.api.FeedResponse
import com.tomclaw.appsend.screen.feed.api.ReadResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface FeedInteractor {

    fun readFeed(postId: Int): Observable<ReadResponse>

    fun listFeed(
        userId: Int?,
        postId: Int? = null,
        direction: FeedDirection?,
    ): Observable<FeedResponse>

    fun deletePost(postId: Int): Observable<DeletePostResponse>

    fun reaction(tag: String, reactId: String): Observable<FeedReactionResponse>

}

class FeedInteractorImpl(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : FeedInteractor {

    override fun readFeed(postId: Int): Observable<ReadResponse> {
        return api
            .readFeed(postId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun listFeed(
        userId: Int?,
        postId: Int?,
        direction: FeedDirection?,
    ): Observable<FeedResponse> {
        return api
            .getFeedList(userId, postId, direction?.value)
            .map { list ->
                list.result
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun deletePost(postId: Int): Observable<DeletePostResponse> {
        return api
            .deletePost(postId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun reaction(tag: String, reactId: String): Observable<FeedReactionResponse> {
        return api
            .feedReaction(tag, reactId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}

enum class FeedDirection(val value: String) {
    Before("before"),
    After("after"),
    Both("both"),
}
