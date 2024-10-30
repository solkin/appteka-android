package com.tomclaw.appsend.screen.users

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.users.adapter.ItemListener
import com.tomclaw.appsend.screen.users.api.UserEntity
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface UsersPresenter : ItemListener {

    fun attachView(view: UsersView)

    fun detachView()

    fun attachRouter(router: SubscribersRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onUpdate()

    fun invalidateApps()

    interface SubscribersRouter {

        fun openProfileScreen(userId: Int)

        fun leaveScreen()

    }

}

class UsersPresenterImpl(
    private val userId: Int,
    private val interactor: UsersInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val converter: UserConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : UsersPresenter {

    private var view: UsersView? = null
    private var router: UsersPresenter.SubscribersRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<UserItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, UserItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: UsersView) {
        this.view = view

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

    override fun attachRouter(router: UsersPresenter.SubscribersRouter) {
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
        subscriptions += interactor.listUsers(userId)
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

    private fun loadApps(offsetId: Int) {
        subscriptions += interactor.listUsers(userId, offsetId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onLoadMoreError() }
            )
    }

    private fun onLoaded(entities: List<UserEntity>) {
        isError = false
        val newItems = entities
            .map { converter.convert(it) }
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
            }
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun onItemClick(item: Item) {
        val sub = items?.find { it.id == item.id } ?: return
        router?.openProfileScreen(sub.user.userId)
    }

    override fun onLoadMore(item: Item) {
        val sub = items?.find { it.id == item.id } ?: return
        loadApps(sub.id.toInt())
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
