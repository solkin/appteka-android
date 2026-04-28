package com.tomclaw.appsend.screen.avatar_crop

import android.net.Uri
import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface AvatarCropPresenter {

    fun attachView(view: AvatarCropView)

    fun detachView()

    fun attachRouter(router: AvatarCropRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface AvatarCropRouter {

        fun leaveScreen(success: Boolean, croppedUri: Uri?)

    }

}

class AvatarCropPresenterImpl(
    private val sourceUri: Uri,
    private val cacheKey: String,
    private val interactor: AvatarCropInteractor,
    private val resourceProvider: AvatarCropResourceProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?,
) : AvatarCropPresenter {

    private var view: AvatarCropView? = null
    private var router: AvatarCropPresenter.AvatarCropRouter? = null

    // The bitmap is rebuilt from the source URI on every cold start —
    // it would otherwise dwarf the saved-state bundle. Only the loaded
    // flag is persisted so the loader doesn't re-fire while a crop is
    // already in flight after a recreation.
    private var sourceLoaded: Boolean = state?.getBoolean(KEY_SOURCE_LOADED, false) ?: false

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: AvatarCropView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.doneClicks().subscribe { onDone() }

        if (!sourceLoaded) {
            loadSource()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: AvatarCropPresenter.AvatarCropRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putBoolean(KEY_SOURCE_LOADED, sourceLoaded)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false, croppedUri = null)
    }

    private fun loadSource() {
        view?.showProgress()
        subscriptions += interactor.loadBitmap(sourceUri)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { bitmap ->
                    sourceLoaded = true
                    view?.setBitmap(bitmap)
                    view?.showContent()
                },
                {
                    view?.showContent()
                    view?.showError(resourceProvider.getLoadFailedError())
                    router?.leaveScreen(success = false, croppedUri = null)
                },
            )
    }

    private fun onDone() {
        val view = view ?: return
        val cropped = view.readCroppedBitmap()
        if (cropped == null) {
            view.showError(resourceProvider.getSaveFailedError())
            return
        }
        view.showProgress()
        subscriptions += interactor.saveBitmap(cropped, cacheKey)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { uri ->
                    view.showContent()
                    router?.leaveScreen(success = true, croppedUri = uri)
                },
                {
                    view.showContent()
                    view.showError(resourceProvider.getSaveFailedError())
                },
            )
    }

}

private const val KEY_SOURCE_LOADED = "source_loaded"
