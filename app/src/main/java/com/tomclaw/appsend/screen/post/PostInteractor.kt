package com.tomclaw.appsend.screen.post

import android.annotation.SuppressLint
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.post.api.FeedPostResponse
import com.tomclaw.appsend.screen.post.dto.PostScreenshot
import com.tomclaw.appsend.upload.UploadScreenshotsResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import okhttp3.MultipartBody

interface PostInteractor {

    fun uploadScreenshots(scrs: List<PostScreenshot>): Observable<UploadScreenshotsResponse>

    fun post(text: String, scrIds: List<String>): Observable<FeedPostResponse>

}

class PostInteractorImpl(
    private val api: StoreApi,
    private val compressor: ImageCompressor,
    private val schedulers: SchedulersFactory
) : PostInteractor {

    @SuppressLint("DefaultLocale")
    override fun uploadScreenshots(scrs: List<PostScreenshot>): Observable<UploadScreenshotsResponse> {
        return api
            .uploadScreenshots(
                images = scrs.map { scr ->
                    val uri = scr.original
                    val name = String.format(format = "src%d.jpg", uri.hashCode())
                    MultipartBody.Part.createFormData(
                        "images",
                        name,
                        compressor.asRequestBody(uri)
                    )
                }
            )
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
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
