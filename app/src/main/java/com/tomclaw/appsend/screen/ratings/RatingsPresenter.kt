package com.tomclaw.appsend.screen.ratings

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.screen.ratings.adapter.ItemListener
import com.tomclaw.appsend.screen.ratings.adapter.rating.RatingItem
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.RoleHelper
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.plusAssign

interface RatingsPresenter : ItemListener {

    fun attachView(view: RatingsView)

    fun detachView()

    fun attachRouter(router: RatingsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    fun invalidateApps()

    interface RatingsRouter {

        fun openUserProfile(userId: Int)

        fun leaveScreen()

    }

}

class RatingsPresenterImpl(
    private val interactor: RatingsInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val converter: RatingConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : RatingsPresenter {

    private var view: RatingsView? = null
    private var router: RatingsPresenter.RatingsRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<RatingItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, RatingItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false
    private var brief: UserBrief? = state?.getParcelableCompat(KEY_BRIEF, UserBrief::class.java)

    override fun attachView(view: RatingsView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.retryClicks().subscribe {
            loadRatings()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
        }

        if (isError) {
            onError()
            onReady()
        } else {
            items?.let { onReady() } ?: loadRatings()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: RatingsPresenter.RatingsRouter) {
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
        loadRatings()
    }

    private fun loadRatings() {
        subscriptions += Observables
            .zip(
                interactor.listRatings(offsetRateId = null),
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

    private fun loadRatings(offsetRateId: Int) {
        subscriptions += interactor.listRatings(offsetRateId)
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

    private fun onLoaded(entities: List<RatingEntity>) {
        isError = false
        val newItems = entities
            .map { converter.convert(it, brief) }
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
        val review = items?.find { it.id == item.id } ?: return
        router?.openUserProfile(review.userId)
    }

    override fun onDeleteClick(item: Item) {
        val review = items?.find { it.id == item.id } ?: return
        subscriptions += interactor.deleteRating(review.rateId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady() }
            .subscribe(
                { onRatingDeleted(item) },
                { view?.showRatingRemovalFailed() }
            )
    }

    private fun onRatingDeleted(item: Item) {
        this.items = items?.filter { it.id != item.id }
        onReady()
    }

    override fun onRetryClick(item: Item) {
        val review = items?.find { it.id == item.id } ?: return
        if (items?.isNotEmpty() == true) {
            items?.last()?.let {
                it.hasProgress = true
                it.hasError = false
            }
            items?.indexOf(review)?.let {
                view?.contentUpdated(it)
            }
        }
        loadRatings(review.rateId)
    }

    override fun onLoadMore(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        loadRatings(app.rateId)
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
private const val KEY_BRIEF = "brief"
