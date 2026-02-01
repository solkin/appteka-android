package com.tomclaw.appsend.screen.post

import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.post.api.FeedPostResponse
import com.tomclaw.appsend.screen.post.dto.PostImage
import com.tomclaw.appsend.screen.post.adapter.ItemListener
import com.tomclaw.appsend.screen.post.adapter.image.ImageItem
import com.tomclaw.appsend.screen.post.dto.FeedConfig
import com.tomclaw.appsend.screen.feed.api.Reaction
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import com.tomclaw.appsend.util.trim
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import kotlin.math.min

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

        fun leaveScreen(postId: Int? = 0)

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

    private var images: MutableList<PostImage> = state
        ?.getParcelableArrayListCompat(KEY_IMAGES, PostImage::class.java) ?: mutableListOf()
    private var text: String = state?.getString(KEY_TEXT).orEmpty()
    private var highlightErrors: Boolean = state?.getBoolean(KEY_HIGHLIGHT_ERRORS) == true
    private var config: FeedConfig? = state?.getParcelableCompat(KEY_CONFIG, FeedConfig::class.java)
    private var selectedReactionIds: MutableSet<String> = state
        ?.getStringArray(KEY_SELECTED_REACTIONS)
        ?.toSet()
        ?.toMutableSet() ?: mutableSetOf()

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()
    private val statusSubscription = CompositeDisposable()

    override fun attachView(view: PostView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.loginClicks().subscribe {
            router?.openLoginScreen()
        }

        if (config != null) {
            invalidate()
        } else {
            loadConfig()
        }
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
        putParcelableArrayList(KEY_IMAGES, ArrayList(images))
        putString(KEY_TEXT, text)
        putBoolean(KEY_HIGHLIGHT_ERRORS, highlightErrors)
        putParcelable(KEY_CONFIG, config)
        putStringArray(KEY_SELECTED_REACTIONS, selectedReactionIds.toTypedArray())
    }

    override fun onAuthorized() {
        invalidate()
    }

    override fun onImagesSelected(images: List<PostImage>) {
        val config = config ?: return
        this.images = (this.images + images)
            .distinctBy { it.original }
            .trim(config.postMaxImages)
            .toMutableList()
        bindForm()
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun loadConfig() {
        subscriptions += interactor.config()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { view?.showContent() }
            .subscribe(
                {
                    config = FeedConfig(
                        postMaxLength = it.postMaxLength,
                        postMaxImages = it.postMaxImages,
                        reactions = it.reactions,
                    )
                    invalidate()
                },
                {}
            )
    }

    private fun postFeed() {
        val imageUploadObservable = if (images.isNotEmpty()) {
            interactor.uploadImages(images).map { it.scrIds }
        } else {
            Observable.just(emptyList<String>())
        }
        subscriptions += imageUploadObservable
            .flatMap { interactor.post(text.trim(), it, selectedReactionIds.toList()) }
            .observeOn(schedulers.mainThread())
            .doOnSubscribe {
                router?.hideKeyboard()
                view?.showProgress()
            }
            .doAfterTerminate { view?.showContent() }
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
        router?.leaveScreen(response.postId)
    }

    private fun bindForm() {
        updateItems()
        bindItems()
        view?.contentUpdated()
    }

    private fun updateItems() {
        val config = config ?: return
        items.clear()
        items += postConverter.convert(images, text, highlightErrors, config, selectedReactionIds)
    }

    private fun bindItems() {
        adapterPresenter.get().onDataSourceChanged(items)
    }

    private fun invalidate() {
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

    override fun onReactionClick(reaction: Reaction) {
        if (selectedReactionIds.contains(reaction.id)) {
            selectedReactionIds.remove(reaction.id)
        } else {
            selectedReactionIds.add(reaction.id)
        }
        bindForm()
    }

}

private const val KEY_IMAGES = "images"
private const val KEY_TEXT = "text"
private const val KEY_HIGHLIGHT_ERRORS = "highlight_errors"
private const val KEY_CONFIG = "config"
private const val KEY_SELECTED_REACTIONS = "selected_reactions"
