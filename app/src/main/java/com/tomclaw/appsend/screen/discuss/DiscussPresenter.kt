package com.tomclaw.appsend.screen.discuss

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.discuss.adapter.ItemListener
import com.tomclaw.appsend.screen.discuss.adapter.topic.TopicItem
import com.tomclaw.appsend.screen.discuss.api.TopicEntry
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface DiscussPresenter : ItemListener {

    fun attachView(view: DiscussView)

    fun detachView()

    fun attachRouter(router: DiscussPresenter.DiscussRouter)

    fun detachRouter()

    fun saveState(): Bundle

    interface DiscussRouter {
    }

}

class DiscussPresenterImpl(
    private val converter: TopicConverter,
    private val preferences: DiscussPreferencesProvider,
    private val interactor: DiscussInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DiscussPresenter {

    private var view: DiscussView? = null
    private var router: DiscussPresenter.DiscussRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<TopicItem>? = state?.getParcelableArrayList(KEY_TOPICS)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: DiscussView) {
        this.view = view

        view.getStartedClicks().subscribe {
            preferences.setIntroShown()
            loadTopics()
        }

        view.retryButtonClicks().subscribe {
            loadTopics()
        }

        if (preferences.isShowIntro()) {
            view.showIntro()
        } else {
            if (isError) {
                onError()
                onReady()
            } else {
                items?.let { onReady() } ?: loadTopics()
            }
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: DiscussPresenter.DiscussRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_TOPICS, items?.let { ArrayList(items.orEmpty()) })
        putBoolean(KEY_ERROR, isError)
    }

    private fun loadTopics() {
        interactor.listTopics()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun loadTopics(offset: Int) {
        subscriptions += interactor.listTopics(offset)
            .observeOn(schedulers.mainThread())
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onLoadMoreError() }
            )
    }

    private fun onLoaded(entities: List<TopicEntry>) {
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
        val items = this.items.takeIf { !it.isNullOrEmpty() } ?: emptyList()
        when {
            isError -> {
                view?.showError()
            }
            else -> {
                val dataSource = ListDataSource(items)
                adapterPresenter.get().onDataSourceChanged(dataSource)
                view?.let {
                    it.contentUpdated()
                    it.showContent()
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

    override fun onItemClick(item: Item) {
        TODO("Not yet implemented")
    }

    override fun onRetryClick(item: Item) {
        TODO("Not yet implemented")
    }

    override fun onLoadMore(item: Item) {
        val offset = items?.size ?: return
        loadTopics(offset)
    }

}

private const val KEY_TOPICS = "topics"
private const val KEY_ERROR = "error"
