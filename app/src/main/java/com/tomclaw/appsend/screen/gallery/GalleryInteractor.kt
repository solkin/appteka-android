package com.tomclaw.appsend.screen.gallery

import android.net.Uri
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Request.Builder


interface GalleryInteractor {

    fun downloadFile(source: Uri, destination: Uri): Single<Unit>

}

class GalleryInteractorImpl(
    private val client: OkHttpClient,
    private val streamsProvider: StreamsProvider,
    private val schedulers: SchedulersFactory
) : GalleryInteractor {

    override fun downloadFile(source: Uri, destination: Uri): Single<Unit> {
        return Single
            .create { emitter ->
                val request: Request = Builder().url(source.toString()).build()
                client.newCall(request).execute().use { response ->
                    response.body()?.byteStream()?.let { input ->
                        streamsProvider.openOutputStream(destination)?.let { output ->
                            input.copyTo(output)
                            output.flush()
                            emitter.onSuccess(Unit)
                        } ?: emitter.onError(Throwable("Output stream opening error"))
                    } ?: emitter.onError(Throwable("Screenshot response error"))
                }
            }
            .subscribeOn(schedulers.io())
    }

}
