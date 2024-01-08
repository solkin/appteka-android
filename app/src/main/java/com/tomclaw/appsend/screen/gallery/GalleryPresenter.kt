package com.tomclaw.appsend.screen.gallery

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.gallery.adapter.image.ImageItem
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

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
    startIndex: Int,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : GalleryPresenter {

    private var view: GalleryView? = null
    private var router: GalleryPresenter.GalleryRouter? = null

    private var index: Int = state?.getInt(KEY_CURRENT_INDEX) ?: startIndex

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: GalleryView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.activeChanged().subscribe { index = it }

        bindItems()
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
        putInt(KEY_CURRENT_INDEX, index)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun bindItems() {
        val dataSource = ListDataSource(
            items.mapIndexed { index, item ->
                ImageItem(index.toLong(), item.uri)
            }
        )
        adapterPresenter.get().onDataSourceChanged(dataSource)

        view?.setCurrentIndex(index)
    }

}

private const val KEY_CURRENT_INDEX = "index"
