package com.tomclaw.appsend.screen.post

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.post.api.FeedPostResponse
import com.tomclaw.appsend.screen.post.dto.PostScreenshot
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import java.util.Locale

interface PostInteractor {

    fun uploadScreenshots(scr: List<PostScreenshot>): Observable<List<String>>

    fun post(text: String, scrIds: List<String>): Observable<FeedPostResponse>

}

class PostInteractorImpl(
    private val api: StoreApi,
    private val locale: Locale,
    private val schedulers: SchedulersFactory
) : PostInteractor {

    override fun uploadScreenshots(scr: List<PostScreenshot>): Observable<List<String>> {
        TODO("Not yet implemented")
    }

    override fun post(text: String, scrIds: List<String>): Observable<FeedPostResponse> {
        return api
            .postFeed(
                text = text,
                scrIds = scrIds,
            )
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
