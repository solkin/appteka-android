package com.tomclaw.appsend.screen.chat

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntry
import com.tomclaw.appsend.screen.chat.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface ChatPresenter : ItemListener {

    fun attachView(view: ChatView)

    fun detachView()

    fun attachRouter(router: ChatRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface ChatRouter {

        fun leaveScreen()

    }

}

class ChatPresenterImpl(
    private val topicId: Int,
    private val converter: MessageConverter,
    private val interactor: ChatInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ChatPresenter {

    private var view: ChatView? = null
    private var router: ChatPresenter.ChatRouter? = null

    private var topic: TopicEntry? = state?.getParcelable(KEY_TOPIC)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false
    private var history: List<MessageEntity>? = state?.getParcelableArrayList(KEY_HISTORY)

    private val journal = HashSet<Int>()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: ChatView) {
        this.view = view

        view.navigationClicks().subscribe {
            onBackPressed()
        }

        view.retryClicks().subscribe {
        }

        when {
            isError -> {
                onTopicError()
            }
            topic != null -> {
                onTopicLoaded()
            }
            else -> {
                loadTopic()
            }
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: ChatPresenter.ChatRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelable(KEY_TOPIC, topic)
        putBoolean(KEY_ERROR, isError)
        history?.let { putParcelableArrayList(KEY_HISTORY, ArrayList(it)) }
    }

    private fun loadTopic() {
        subscriptions += interactor.getTopic(topicId)
            .flatMap { topic ->
                this.topic = topic
                interactor.loadHistory(topicId, 0, topic.lastMsg.msgId)
            }
            .map { this.history = it }
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                { onTopicLoaded() },
                { onTopicError() }
            )
    }

    private fun onTopicLoaded() {
        val topic = topic ?: return
        isError = false
        view?.setTitle(topic.title)

        val items = convertHistory()

        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)

        view?.contentUpdated()
        view?.showContent()
    }

    private fun onTopicError() {
        isError = true
        view?.showError()
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onItemClick(item: Item) {
    }

    override fun onLoadMore(msgId: Int) {
        if (history?.last()?.msgId == msgId && journal.add(msgId)) {
            subscriptions += interactor.loadHistory(topicId, 0, msgId)
                .observeOn(schedulers.mainThread())
                .doOnSubscribe { view?.showProgress() }
                .doAfterTerminate { journal.remove(msgId) }
                .subscribe(
                    { onHistoryLoaded(it) },
                    { onHistoryError() }
                )
        }
    }

    private fun onHistoryLoaded(list: List<MessageEntity>) {
        val countBefore = history?.size ?: 0
        history = (history ?: emptyList()) + list

        val items = convertHistory()

        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)

        view?.contentRangeInserted(countBefore, list.size)
        view?.showContent()
    }

    private fun onHistoryError() {
        view?.showContent()
    }

    private fun convertHistory(): List<Item> {
        val history = history ?: emptyList()
        var prevMsg: MessageEntity? = null
        return history
            .map {
                val item = converter.convert(it, prevMsg)
                prevMsg = it
                item
            }
            .toList()
    }

}

private const val KEY_TOPIC = "topic"
private const val KEY_ERROR = "error"
private const val KEY_HISTORY = "history"
