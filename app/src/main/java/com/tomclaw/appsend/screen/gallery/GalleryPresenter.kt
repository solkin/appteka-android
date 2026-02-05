package com.tomclaw.appsend.screen.gallery

import android.net.Uri
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.screen.gallery.adapter.image.ImageItem
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.crc32
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
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

    fun onSaveCurrentScreenshot(uri: Uri)

    interface GalleryRouter {

        fun openSaveScreenshotDialog(fileName: String, fileType: String)

        fun leaveScreen(success: Boolean)

    }

}

class GalleryPresenterImpl(
    private val items: List<GalleryItem>,
    startIndex: Int,
    private val resourceProvider: GalleryResourceProvider,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val interactor: GalleryInteractor,
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
        subscriptions += view.downloadClicks().subscribe {
            val item = items[pageIndex]
            router?.openSaveScreenshotDialog(
                fileName = getSimpleFileName(uri = item.uri),
                fileType = "image/jpeg"
            )
        }

        bindItems()
    }

    private fun getSimpleFileName(uri: Uri): String {
        val url = uri.toString()
        return String.format("%08x", url.crc32()) + ".jpg"
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

    override fun onSaveCurrentScreenshot(uri: Uri) {
        val source = items[pageIndex].uri
        subscriptions += interactor.downloadFile(source, destination = uri)
            .toObservable()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { },
                { view?.showError(resourceProvider.errorSavingScreenshot()) }
            )
    }

    private fun bindItems() {
        val imageItems = items.mapIndexed { index, item ->
            ImageItem(id = index.toLong(), uri = item.uri)
        }
        adapterPresenter.get().onDataSourceChanged(imageItems)

        view?.setCurrentIndex(pageIndex)
        bindPageIndex()
    }

}

private const val KEY_PAGE_INDEX = "page_index"
