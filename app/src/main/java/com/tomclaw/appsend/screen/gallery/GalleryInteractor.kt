package com.tomclaw.appsend.screen.gallery

import android.net.Uri
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Single


interface GalleryInteractor {

    fun downloadFile(source: Uri, destination: Uri): Single<Unit>

}

class GalleryInteractorImpl(
    private val streamsProvider: StreamsProvider,
    private val schedulers: SchedulersFactory
) : GalleryInteractor {

    override fun downloadFile(source: Uri, destination: Uri): Single<Unit> {
        return Single
            .create { emitter ->
                 streamsProvider.openInputStream(source)?.let { input ->
                    streamsProvider.openOutputStream(destination)?.let { output ->
                        input.copyTo(output)
                        output.flush()
                        emitter.onSuccess(Unit)
                    } ?: emitter.onError(Throwable("Output stream opening error"))
                } ?: emitter.onError(Throwable("Input stream opening error"))
            }
            .subscribeOn(schedulers.io())
    }

}
