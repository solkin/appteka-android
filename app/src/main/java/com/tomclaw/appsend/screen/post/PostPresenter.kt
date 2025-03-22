package com.tomclaw.appsend.screen.post

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.post.api.FeedPostResponse
import com.tomclaw.appsend.screen.post.dto.PostScreenshot
import com.tomclaw.appsend.screen.post.adapter.ItemListener
import com.tomclaw.appsend.screen.post.adapter.screen_image.ScreenImageItem
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface PostPresenter : ItemListener {

    fun attachView(view: PostView)

    fun detachView()

    fun attachRouter(router: PostRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onAuthorized()

    fun onImagesSelected(images: List<PostScreenshot>)

    fun onBackPressed()

    interface PostRouter {

        fun openLoginScreen()

        fun openImagePicker()

        fun openGallery(items: List<GalleryItem>, current: Int)

        fun leaveScreen()

        fun hideKeyboard()

    }

}

class PostPresenterImpl(
    private val interactor: PostInteractor,
    private val postConverter: PostConverter,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val preferences: PostPreferencesProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : PostPresenter {

    private var view: PostView? = null
    private var router: PostPresenter.PostRouter? = null

    private var screenshots: ArrayList<PostScreenshot> = state
        ?.getParcelableArrayListCompat(KEY_SCREENSHOTS, PostScreenshot::class.java) ?: ArrayList()
    private var text: String = state?.getString(KEY_TEXT).orEmpty()

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()
    private val statusSubscription = CompositeDisposable()

    override fun attachView(view: PostView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.loginClicks().subscribe {
            router?.openLoginScreen()
        }

        invalidate()
    }

    override fun detachView() {
        subscriptions.clear()
        statusSubscription.clear()
        this.view = null
    }

    override fun attachRouter(router: PostPresenter.PostRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_SCREENSHOTS, screenshots)
        putString(KEY_TEXT, text)
    }

    override fun onAuthorized() {
        invalidate()
    }

    override fun onImagesSelected(images: List<PostScreenshot>) {
        screenshots = ArrayList((screenshots + images).distinctBy { it.original })
        bindForm()
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun postFeed() {
        subscriptions += interactor.uploadScreenshots(screenshots)
            .flatMap { interactor.post(text, it.scrIds) }
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.showProgress()
            }
            .subscribe(
                { onPostDone(it) },
                {
                    it.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
                        view?.showPostError()
                    }
                }
            )
    }

    private fun onPostDone(response: FeedPostResponse) {
        router?.leaveScreen()
    }

    private fun updateItems() {
        items.clear()
        items += postConverter.convert(screenshots, text)
    }

    private fun bindForm() {
        updateItems()
        bindItems()
        view?.contentUpdated()
    }

    private fun bindItems() {
        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
    }

    private fun invalidate() {
        bindForm()
    }

    private fun clearForm() {
        screenshots = ArrayList()
        text = ""
        bindForm()
    }

    override fun onTextChanged(text: String) {
        this.text = text
        updateItems()
    }

    override fun onSubmitClick() {
        postFeed()
    }

    override fun onScreenAppendClick() {
        router?.openImagePicker()
    }

    override fun onScreenshotClick(item: ScreenImageItem) {
        router?.openGallery(
            items = screenshots.map { GalleryItem(it.original, it.width, it.height) },
            current = screenshots.indexOfFirst { it.original == item.original },
        )
    }

    override fun onScreenshotDelete(item: ScreenImageItem) {
        screenshots.remove(screenshots.first { it.original == item.original })
        bindForm()
    }

}

private const val KEY_SCREENSHOTS = "screenshots"
private const val KEY_TEXT = "text"
