package com.tomclaw.appsend.screen.post

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.post.api.FeedPostResponse
import com.tomclaw.appsend.screen.post.dto.PostImage
import com.tomclaw.appsend.screen.post.adapter.ItemListener
import com.tomclaw.appsend.screen.post.adapter.image.ImageItem
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

    fun onImagesSelected(images: List<PostImage>)

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

    private var images: ArrayList<PostImage> = state
        ?.getParcelableArrayListCompat(KEY_IMAGES, PostImage::class.java) ?: ArrayList()
    private var text: String = state?.getString(KEY_TEXT).orEmpty()
    private var highlightErrors: Boolean = state?.getBoolean(KEY_HIGHLIGHT_ERRORS) == true

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
        putParcelableArrayList(KEY_IMAGES, images)
        putString(KEY_TEXT, text)
        putBoolean(KEY_HIGHLIGHT_ERRORS, highlightErrors)
    }

    override fun onAuthorized() {
        invalidate()
    }

    override fun onImagesSelected(images: List<PostImage>) {
        this@PostPresenterImpl.images = ArrayList((this@PostPresenterImpl.images + images).distinctBy { it.original })
        bindForm()
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun postFeed() {
        subscriptions += interactor.uploadImages(images)
            .flatMap { interactor.post(text.trim(), it.scrIds) }
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
        items += postConverter.convert(images, text, highlightErrors)
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
        images = ArrayList()
        text = ""
        highlightErrors = false
        bindForm()
    }

    override fun onTextChanged(text: String) {
        this.text = text
        updateItems()
    }

    override fun onSubmitClick() {
        highlightErrors = true
        if (isConditionReady()) {
            postFeed()
        }
        bindForm()
    }

    private fun isConditionReady(): Boolean {
        return text.isNotBlank()
    }

    override fun onScreenAppendClick() {
        router?.openImagePicker()
    }

    override fun onImageClick(item: ImageItem) {
        router?.openGallery(
            items = images.map { GalleryItem(it.original, it.width, it.height) },
            current = images.indexOfFirst { it.original == item.original },
        )
    }

    override fun onImageDelete(item: ImageItem) {
        images.remove(images.first { it.original == item.original })
        bindForm()
    }

}

private const val KEY_IMAGES = "images"
private const val KEY_TEXT = "text"
private const val KEY_HIGHLIGHT_ERRORS = "highlight_errors"
