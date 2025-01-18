package com.tomclaw.appsend.screen.reviews

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.reviews.adapter.ItemListener
import com.tomclaw.appsend.screen.reviews.adapter.review.ReviewItem
import com.tomclaw.appsend.screen.reviews.api.ReviewEntity
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.plusAssign

interface ReviewsPresenter : ItemListener {

    fun attachView(view: ReviewsView)

    fun detachView()

    fun attachRouter(router: ReviewsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    fun invalidateApps()

    interface ReviewsRouter {

        fun openAppScreen(appId: String, title: String)

        fun leaveScreen()

    }

}

class ReviewsPresenterImpl(
    private val interactor: ReviewsInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val converter: ReviewConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ReviewsPresenter {

    private var view: ReviewsView? = null
    private var router: ReviewsPresenter.ReviewsRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<ReviewItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, ReviewItem::class.java)
    private var brief: UserBrief? = state?.getParcelableCompat(KEY_BRIEF, UserBrief::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: ReviewsView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.retryClicks().subscribe {
            loadReviews()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
        }

        if (isError) {
            onError()
            onReady()
        } else {
            items?.let { onReady() } ?: loadReviews()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: ReviewsPresenter.ReviewsRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putParcelable(KEY_BRIEF, brief)
        putBoolean(KEY_ERROR, isError)
    }

    override fun invalidateApps() {
        items = null
        loadReviews()
    }

    private fun loadReviews() {
        subscriptions += Observables
            .zip(
                interactor.listReviews(offsetRateId = null),
                interactor.getUserBrief()
            )
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
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

    private fun loadReviews(offsetRateId: Int) {
        subscriptions += interactor.listReviews(offsetRateId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady() }
            .subscribe(
                {
                    onLoaded(it)
                },
                { onLoadMoreError() }
            )
    }

    private fun onBriefLoaded(brief: UserBrief?) {
        this.brief = brief
    }

    private fun onLoaded(entities: List<ReviewEntity>) {
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
        router?.openAppScreen(review.appId, review.title)
    }

    override fun onDeleteClick(item: Item) {
        val review = items?.find { it.id == item.id } ?: return
        subscriptions += interactor.deleteRating(review.rateId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { onReady() }
            .subscribe(
                { onReviewDeleted(item) },
                { view?.showReviewRemovalFailed() }
            )
    }

    private fun onReviewDeleted(item: Item) {
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
        loadReviews(review.rateId)
    }

    override fun onLoadMore(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        loadReviews(app.rateId)
    }

}

private const val KEY_APPS = "apps"
private const val KEY_BRIEF = "brief"
private const val KEY_ERROR = "error"
