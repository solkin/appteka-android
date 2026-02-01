package com.tomclaw.appsend.screen.topics

import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.topics.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface TopicsPresenter : ItemListener {

    fun attachView(view: TopicsView)

    fun detachView()

    fun attachRouter(router: TopicsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun scrollToTop()

    fun invalidate()

    interface TopicsRouter {

        fun showChatScreen(entity: TopicEntity)

        fun openLoginScreen()

    }

}

class TopicsPresenterImpl(
    private val converter: TopicConverter,
    private val preferences: TopicsPreferencesProvider,
    private val topicsInteractor: TopicsInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : TopicsPresenter {

    private var view: TopicsView? = null
    private var router: TopicsPresenter.TopicsRouter? = null

    private val subscriptions = CompositeDisposable()

    private var entities: List<TopicEntity>? =
        state?.getParcelableArrayListCompat(KEY_TOPICS, TopicEntity::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true
    private var hasMore: Boolean = state?.getBoolean(KEY_HAS_MORE) == true

    override fun attachView(view: TopicsView) {
        this.view = view

        subscriptions += view.getStartedClicks().subscribe {
            preferences.setIntroShown()
            loadTopics()
        }

        subscriptions += view.retryButtonClicks().subscribe {
            loadTopics()
        }

        subscriptions += view.refreshClicks().subscribe {
            invalidateTopics()
        }

        subscriptions += view.pinTopicClicks().subscribe { topicId ->
            pinTopic(topicId)
        }

        subscriptions += view.loginClicks().subscribe {
            router?.openLoginScreen()
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

    override fun scrollToTop() {
        view?.scrollToTop()
    }

    override fun invalidate() {
        if (!preferences.isShowIntro()) {
            invalidateTopics()
        }
    }

    private fun invalidateTopics() {
        entities = null
        isError = false
        loadTopics()
    }

    private fun loadTopics() {
        subscriptions += topicsInteractor.listTopics()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe {
                entities = null
                if (view?.isPullRefreshing() == false) view?.showProgress()
            }
            .subscribe(
                { onLoaded(it.topics, it.hasMore) },
                { onError() }
            )
    }

    private fun loadTopics(offset: Int) {
        subscriptions += topicsInteractor.listTopics(offset)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { onLoaded(it.topics, it.hasMore) },
                { onError() }
            )
    }

    private fun onLoaded(entities: List<TopicEntity>, hasMore: Boolean) {
        isError = false
        this.entities = (this.entities ?: emptyList()).plus(entities)
        this.hasMore = hasMore
        bindEntities()
    }

    private fun bindEntities() {
        val entities = this.entities ?: emptyList()

        val items = entities
            .map { converter.convert(it) }
            .apply { if (isNotEmpty()) last().hasMore = hasMore }

        adapterPresenter.get().onDataSourceChanged(items)
        view?.let {
            it.contentUpdated()
            it.stopPullRefreshing()
            it.showContent()
        }
    }

    private fun onError() {
        this.isError = true
        view?.stopPullRefreshing()
        view?.showError()
    }

    private fun pinTopic(topicId: Int) {
        subscriptions += topicsInteractor.pinTopic(topicId)
            .flatMap { topicsInteractor.listTopics() }
            .observeOn(schedulers.mainThread())
            .subscribe(
                { response ->
                    entities = response.topics
                    hasMore = response.hasMore
                    bindEntities()
                },
                { ex ->
                    ex.filterUnauthorizedErrors(
                        authError = { view?.showUnauthorizedError() },
                        other = { view?.showPinFailed() }
                    )
                }
            )
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
