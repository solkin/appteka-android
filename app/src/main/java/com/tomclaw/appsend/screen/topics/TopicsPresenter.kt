package com.tomclaw.appsend.screen.topics

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.events.EventsInteractor
import com.tomclaw.appsend.screen.topics.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit

interface TopicsPresenter : ItemListener {

    fun attachView(view: TopicsView)

    fun detachView()

    fun attachRouter(router: TopicsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    interface TopicsRouter {

        fun showChatScreen(entity: TopicEntity)

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

    private var entities: List<TopicEntity>? = state?.getParcelableArrayList(KEY_TOPICS)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false
    private var hasMore: Boolean = state?.getBoolean(KEY_HAS_MORE) ?: false

    override fun attachView(view: TopicsView) {
        this.view = view

        subscriptions += view.getStartedClicks().subscribe {
            preferences.setIntroShown()
            loadTopics()
        }

        subscriptions += view.retryButtonClicks().subscribe {
            loadTopics()
        }

        subscriptions += view.pinTopicClicks().subscribe { topicId ->
            pinTopic(topicId)
        }

        if (preferences.isShowIntro()) {
            view.showIntro()
        } else {
            if (isError) {
                onError()
            } else {
                entities?.let { bindEntities() } ?: loadTopics()
            }
        }

        subscriptions += eventsInteractor.subscribeOnEvents()
            .observeOn(schedulers.mainThread())
            .subscribe { response ->
                println("[polling] event received (topics)")
                response.topics?.let { topics ->
                    val isInvalidateTopics = response.invalidateTopics ?: false
                    val topItems = ArrayList(topics)
                    val filteredEntities = ArrayList(
                        entities?.takeUnless { isInvalidateTopics } ?: emptyList()
                    )
                    topics.forEach { topic ->
                        filteredEntities.removeAll { it.topicId == topic.topicId }
                    }
                    val newEntities = topItems + filteredEntities
                    entities = newEntities
                    hasMore = hasMore || isInvalidateTopics

                    bindEntities()
                }
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
        putParcelableArrayList(KEY_TOPICS, entities?.let { ArrayList(entities.orEmpty()) })
        putBoolean(KEY_ERROR, isError)
        putBoolean(KEY_HAS_MORE, hasMore)
    }

    private fun loadTopics() {
        subscriptions += topicsInteractor.listTopics()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe {
                entities = null
                view?.showProgress()
            }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun loadTopics(offset: Int) {
        subscriptions += topicsInteractor.listTopics(offset)
            .observeOn(schedulers.mainThread())
            .retryWhen { errors ->
                errors.flatMap {
                    println("[topics] Retry after exception: " + it.message)
                    Observable.timer(3, TimeUnit.SECONDS)
                }
            }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun onLoaded(entities: List<TopicEntity>) {
        isError = false
        hasMore = entities.isNotEmpty()
        this.entities = (this.entities ?: emptyList()).plus(entities)
        bindEntities()
    }

    private fun bindEntities() {
        val entities = this.entities ?: emptyList()

        val items = entities
            .map { converter.convert(it) }
            .apply { if (isNotEmpty()) last().hasMore = hasMore }

        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view?.let {
            it.contentUpdated()
            it.showContent()
        }
    }

    private fun onError() {
        this.isError = true
        view?.showError()
    }

    private fun pinTopic(topicId: Int) {
        subscriptions += topicsInteractor.pinTopic(topicId)
            .observeOn(schedulers.mainThread())
            .subscribe({ }, { view?.showPinFailed() })
    }

    override fun onItemClick(item: Item) {
        val entity = entities?.find { it.topicId.toLong() == item.id } ?: return
        router?.showChatScreen(entity)
    }

    override fun onItemLongClick(item: Item) {
        val entity = entities?.find { it.topicId.toLong() == item.id } ?: return
        view?.showMessageDialog(entity.topicId, entity.isPinned)
    }

    override fun onLoadMore(item: Item) {
        val offset = entities?.size ?: return
        loadTopics(offset)
    }

}

private const val KEY_TOPICS = "topics"
private const val KEY_ERROR = "error"
private const val KEY_HAS_MORE = "has_more"
