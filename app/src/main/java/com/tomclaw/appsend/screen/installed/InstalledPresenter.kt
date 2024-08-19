package com.tomclaw.appsend.screen.installed

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.net.AppEntry
import com.tomclaw.appsend.screen.installed.adapter.ItemListener
import com.tomclaw.appsend.screen.installed.adapter.app.AppItem
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import dagger.Lazy
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.plusAssign

interface InstalledPresenter : ItemListener {

    fun attachView(view: InstalledView)

    fun detachView()

    fun attachRouter(router: InstalledRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    fun invalidateApps()

    interface InstalledRouter {

        fun openAppScreen(appId: String, title: String)

        fun leaveScreen()

    }

}

class InstalledPresenterImpl(
    private val preferencesProvider: InstalledPreferencesProvider,
    private val interactor: InstalledInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : InstalledPresenter {

    private var view: InstalledView? = null
    private var router: InstalledPresenter.InstalledRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: InstalledView) {
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
            onError(throwable = null)
            onReady()
        } else {
            items?.let { onReady() } ?: loadApps()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: InstalledPresenter.InstalledRouter) {
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
        subscriptions += interactor.listInstalledApps(preferencesProvider.isShowSystemApps())
            .flatMap { installed ->
                val installedMap = installed.associate { it.packageName to it.verCode }
                Observables.zip(
                    Single.just(installed).toObservable(),
                    interactor.getUpdates(installedMap)
                )
            }
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it.first, it.second) },
                { onError(it) }
            )
    }

    private fun onLoaded(entities: List<InstalledAppEntity>, updates: List<AppEntry>) {
        isError = false
        val updatesMap = updates.associateBy { it.packageName }
        val newItems = entities
            .map { appConverter.convert(it, updatesMap[it.packageName]) }
            .toList()
        this.items = this.items
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

    private fun onError(throwable: Throwable?) {
        this.isError = true
        throwable?.printStackTrace()
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun onItemClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
//        router?.openAppScreen(app.appId, app.title)
    }

    override fun onUpdateClick(title: String, appId: String) {
        router?.openAppScreen(appId, title)
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
