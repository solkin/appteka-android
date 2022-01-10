package com.tomclaw.appsend.screen.topics

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.topics.adapter.ItemListener
import com.tomclaw.appsend.screen.topics.adapter.topic.TopicItem
import com.tomclaw.appsend.dto.TopicEntry
import com.tomclaw.appsend.events.EventsInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface TopicsPresenter : ItemListener {

    fun attachView(view: TopicsView)

    fun detachView()

    fun attachRouter(router: TopicsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    interface TopicsRouter {

        fun showChatScreen(topicId: Int, title: String)

    }

}

class TopicsPresenterImpl(
    private val converter: TopicConverter,
    private val preferences: TopicsPreferencesProvider,
    private val topicsInteractor: TopicsInteractor,
    private val eventsInteractor: EventsInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : TopicsPresenter {

    private var view: TopicsView? = null
    private var router: TopicsPresenter.TopicsRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<TopicItem>? = state?.getParcelableArrayList(KEY_TOPICS)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: TopicsView) {
        this.view = view

        subscriptions += view.getStartedClicks().subscribe {
            preferences.setIntroShown()
            loadTopics()
        }

        subscriptions += view.retryButtonClicks().subscribe {
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

        subscriptions += eventsInteractor.subscribeOnEvents().subscribe {
            println("Event received (topics)")
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: TopicsPresenter.TopicsRouter) {
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
        subscriptions += topicsInteractor.listTopics()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun loadTopics(offset: Int) {
        subscriptions += topicsInteractor.listTopics(offset)
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
        val topicItem = items?.find { it.id == item.id } ?: return
        router?.showChatScreen(topicItem.id.toInt(), topicItem.title)
    }

    override fun onRetryClick(item: Item) {
        val offset = items?.size ?: return
        if (items?.isNotEmpty() == true) {
            items?.last()?.let {
                it.hasProgress = true
                it.hasError = false
            }
            items?.last()?.let {
                view?.contentUpdated(offset - 1)
            }
        }
        loadTopics(offset)
    }

    override fun onLoadMore(item: Item) {
        val offset = items?.size ?: return
        loadTopics(offset)
    }

}

private const val KEY_TOPICS = "topics"
private const val KEY_ERROR = "error"
