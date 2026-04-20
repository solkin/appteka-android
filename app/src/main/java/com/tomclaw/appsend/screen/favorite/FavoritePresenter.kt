package com.tomclaw.appsend.screen.favorite

import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.favorite.adapter.ItemListener
import com.tomclaw.appsend.screen.favorite.adapter.app.AppItem
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface FavoritePresenter : ItemListener {

    fun attachView(view: FavoriteView)

    fun detachView()

    fun attachRouter(router: FavoriteRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    fun invalidateApps()

    interface FavoriteRouter {

        fun openAppScreen(appId: String, title: String)

        fun leaveScreen()

    }

}

class FavoritePresenterImpl(
    private val interactor: FavoriteInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : FavoritePresenter {

    private var view: FavoriteView? = null
    private var router: FavoritePresenter.FavoriteRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true

    private var pendingRemoval: PendingRemoval? = null

    override fun attachView(view: FavoriteView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.retryClicks().subscribe {
            loadApps()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
        }
        subscriptions += view.removeSwipes().subscribe { itemId ->
            onRemoveSwipe(itemId)
        }
        subscriptions += view.undoClicks().subscribe {
            onUndoRemove()
        }
        subscriptions += view.removeCommits().subscribe {
            commitPendingRemoval(trackFailure = true)
        }

        if (isError) {
            onError()
            onReady()
        } else {
            items?.let { onReady() } ?: loadApps()
        }
    }

    override fun detachView() {
        commitPendingRemoval(trackFailure = false)
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: FavoritePresenter.FavoriteRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putBoolean(KEY_ERROR, isError)
    }

    override fun invalidateApps() {
        items = null
        loadApps()
    }

    private fun loadApps() {
        subscriptions += interactor.listApps()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                {
                    it.printStackTrace()
                    onError()
                }
            )
    }

    private fun loadApps(offsetAppId: String) {
        subscriptions += interactor.listApps(offsetAppId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onLoadMoreError() }
            )
    }

    private fun onLoaded(entities: List<AppEntity>) {
        isError = false
        val newItems = entities
            .map { appConverter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) last().hasMore = true }
        this.items = this.items
            ?.apply { if (isNotEmpty()) last().hasProgress = false }
            ?.plus(newItems) ?: newItems
    }

    private fun onReady() {
        val items = this.items
        when {
            isError -> {
                view?.showError()
            }

            items.isNullOrEmpty() -> {
                view?.showPlaceholder()
            }

            else -> {
                adapterPresenter.get().onDataSourceChanged(items)
                view?.let {
                    it.contentUpdated()
                    if (it.isPullRefreshing()) {
                        it.stopPullRefreshing()
                    } else {
                        it.showContent()
                    }
                }
            }
        }
    }

    private fun onError() {
        this.isError = true
    }

    private fun onLoadMoreError() {
        items?.last()
            ?.apply {
                hasProgress = false
                hasMore = false
                hasError = true
            }
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun onItemClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        router?.openAppScreen(app.appId, app.title)
    }

    override fun onRetryClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        if (items?.isNotEmpty() == true) {
            items?.last()?.let {
                it.hasProgress = true
                it.hasError = false
            }
            items?.indexOf(app)?.let {
                view?.contentUpdated(it)
            }
        }
        loadApps(app.appId)
    }

    override fun onLoadMore(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        loadApps(app.appId)
    }

    private fun onRemoveSwipe(itemId: Long) {
        commitPendingRemoval(trackFailure = true)
        val current = items.orEmpty()
        val position = current.indexOfFirst { it.id == itemId }
        if (position < 0) return
        val item = current[position]
        val updated = current.toMutableList().apply { removeAt(position) }
        items = updated
        adapterPresenter.get().onDataSourceChanged(updated)
        view?.itemRemoved(position)
        pendingRemoval = PendingRemoval(position, item)
        if (updated.isEmpty()) {
            view?.showPlaceholder()
        }
        view?.showUndoSnackbar()
    }

    private fun onUndoRemove() {
        val pending = pendingRemoval ?: return
        pendingRemoval = null
        val current = items.orEmpty()
        val insertPosition = pending.position.coerceIn(0, current.size)
        val updated = current.toMutableList().apply { add(insertPosition, pending.item) }
        items = updated
        adapterPresenter.get().onDataSourceChanged(updated)
        if (current.isEmpty()) {
            view?.showContent()
            view?.contentUpdated()
        } else {
            view?.itemInserted(insertPosition)
        }
    }

    private fun commitPendingRemoval(trackFailure: Boolean) {
        val pending = pendingRemoval ?: return
        pendingRemoval = null
        val disposable = interactor
            .markFavorite(pending.item.appId, isFavorite = false)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { /* committed */ },
                { error ->
                    error.printStackTrace()
                    if (trackFailure) {
                        onRemoveFailed(pending)
                    }
                }
            )
        if (trackFailure) {
            subscriptions += disposable
        }
    }

    private fun onRemoveFailed(pending: PendingRemoval) {
        val current = items.orEmpty()
        val wasEmpty = current.isEmpty()
        val insertPosition = pending.position.coerceIn(0, current.size)
        val updated = current.toMutableList().apply { add(insertPosition, pending.item) }
        items = updated
        adapterPresenter.get().onDataSourceChanged(updated)
        if (wasEmpty) {
            view?.showContent()
            view?.contentUpdated()
        } else {
            view?.itemInserted(insertPosition)
        }
        view?.showRemoveError()
    }

}

private data class PendingRemoval(
    val position: Int,
    val item: AppItem,
)

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
