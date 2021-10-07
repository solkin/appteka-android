package com.tomclaw.appsend.screen.moderation

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.moderation.adapter.app.AppItem
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.screen.moderation.adapter.ItemListener
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.ArrayList

interface ModerationPresenter : ItemListener {

    fun attachView(view: ModerationView)

    fun detachView()

    fun attachRouter(router: ModerationRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    interface ModerationRouter {

        fun openAppModerationScreen(appId: Int)

        fun leaveScreen()

    }

}

class ModerationPresenterImpl(
    private val interactor: ModerationInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ModerationPresenter {

    private var view: ModerationView? = null
    private var router: ModerationPresenter.ModerationRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? = state?.getParcelableArrayList(KEY_APPS)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: ModerationView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.retryClicks().subscribe {
            loadApps()
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

    override fun attachRouter(router: ModerationPresenter.ModerationRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putBoolean(KEY_ERROR, isError)
    }

    private fun loadApps() {
        subscriptions += interactor.listApps()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun loadApps(offsetAppId: Int) {
        subscriptions += interactor.listApps(offsetAppId)
            .observeOn(schedulers.mainThread())
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onLoadMoreError() }
            )
    }

    private fun onLoaded(entities: List<AppEntity>) {
        val newItems = entities
            .map { appConverter.convert(it) }
            .toList()
            .apply { if (entities.isNotEmpty()) last().hasMore = true }
        this.items = this.items
            ?.apply { if (entities.isNotEmpty()) last().hasProgress = false }
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
                val dataSource = ListDataSource(items)
                adapterPresenter.get().onDataSourceChanged(dataSource)
                view?.contentUpdated()
                view?.showContent()
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
//        val chat = chatIds[item.id] ?: return
//        router?.openModerationScreen(chat.appId)
    }

    override fun onRetryClick(item: Item) {
        TODO("Not yet implemented")
    }

    override fun onLoadMore(item: Item) {
        loadApps(item.id.toInt())
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
