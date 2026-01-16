package com.tomclaw.appsend.screen.downloads

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.downloads.adapter.ItemListener
import com.tomclaw.appsend.screen.downloads.adapter.app.AppItem
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.plusAssign

interface DownloadsPresenter : ItemListener {

    fun attachView(view: DownloadsView)

    fun detachView()

    fun attachRouter(router: DownloadsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    fun invalidateApps()

    interface DownloadsRouter {

        fun openAppScreen(appId: String, title: String)

        fun leaveScreen()

    }

}

class DownloadsPresenterImpl(
    private val userId: Int,
    private val interactor: DownloadsInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DownloadsPresenter {

    private var view: DownloadsView? = null
    private var router: DownloadsPresenter.DownloadsRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true
    private var brief: UserBrief? = state?.getParcelableCompat(KEY_BRIEF, UserBrief::class.java)

    override fun attachView(view: DownloadsView) {
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

        if (isError) {
            onError()
            onReady()
        } else {
            items?.let { onReady() } ?: loadApps()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: DownloadsPresenter.DownloadsRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putBoolean(KEY_ERROR, isError)
        putParcelable(KEY_BRIEF, brief)
    }

    override fun invalidateApps() {
        items = null
        loadApps()
    }

    private fun loadApps() {
        subscriptions += Observables
            .zip(
                interactor.listApps(offsetAppId = null),
                interactor.getUserBrief()
            )
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady() }
            .subscribe(
                {
                    onBriefLoaded(it.second.userBrief)
                    onLoaded(it.first)
                },
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

    private fun onBriefLoaded(brief: UserBrief?) {
        this.brief = brief
    }

    private fun onLoaded(entities: List<AppEntity>) {
        isError = false
        val showMenu = isCurrentUserOwner()
        val newItems = entities
            .map { appConverter.convert(it).apply { this.showMenu = showMenu } }
            .toList()
            .apply { if (isNotEmpty()) last().hasMore = true }
        this.items = this.items
            ?.apply { if (isNotEmpty()) last().hasProgress = false }
            ?.plus(newItems) ?: newItems
    }

    private fun isCurrentUserOwner(): Boolean {
        return brief?.let { it.userId == userId } == true
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
                val dataSource = ListDataSource(items)
                adapterPresenter.get().onDataSourceChanged(dataSource)
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

    override fun onDeleteClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        subscriptions += interactor.deleteDownloaded(app.appId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady() }
            .subscribe(
                { onAppDeleted(item) },
                { view?.showDownloadRemovalFailed() }
            )
    }

    private fun onAppDeleted(item: Item) {
        this.items = items?.filter { it.id != item.id }
        onReady()
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

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
private const val KEY_BRIEF = "brief"
