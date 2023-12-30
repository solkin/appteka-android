package com.tomclaw.appsend.screen.gallery

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable

interface GalleryPresenter {

    fun attachView(view: GalleryView)

    fun detachView()

    fun attachRouter(router: GalleryRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface GalleryRouter {

        fun leaveScreen(success: Boolean)

    }

}

class GalleryPresenterImpl(
    private val items: List<GalleryItem>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : GalleryPresenter {

    private var view: GalleryView? = null
    private var router: GalleryPresenter.GalleryRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: GalleryView) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: GalleryPresenter.GalleryRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

}
