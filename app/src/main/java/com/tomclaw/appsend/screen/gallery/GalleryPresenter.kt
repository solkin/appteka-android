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
    private val resourceProvider: GalleryResourceProvider,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : GalleryPresenter {

    private var view: GalleryView? = null
    private var router: GalleryPresenter.GalleryRouter? = null

    private var pageIndex: Int = state?.getInt(KEY_PAGE_INDEX) ?: startIndex

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: GalleryView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.pageChanged().subscribe { index ->
            this.pageIndex = index
            bindPageIndex()
        }

        bindItems()
    }

    private fun bindPageIndex() {
        view?.setTitle(resourceProvider.formatTitle(pageIndex + 1, items.size))
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
        putInt(KEY_PAGE_INDEX, pageIndex)
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

        view?.setCurrentIndex(pageIndex)
        bindPageIndex()
    }

}

private const val KEY_PAGE_INDEX = "page_index"
