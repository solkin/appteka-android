package com.tomclaw.appsend.screen.chat

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.events.EventsInteractor
import com.tomclaw.appsend.screen.chat.adapter.ItemListener
import com.tomclaw.appsend.util.RoleHelper.ROLE_ADMIN
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

        fun openProfileScreen(userId: Int)

        fun leaveScreen()

    }

}

class ChatPresenterImpl(
    private val topicId: Int,
    private val converter: MessageConverter,
    private val chatInteractor: ChatInteractor,
    private val eventsInteractor: EventsInteractor,
    private val resourceProvider: ChatResourceProvider,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ChatPresenter {

    private var view: ChatView? = null
    private var router: ChatPresenter.ChatRouter? = null

    private var topic: TopicEntity? = state?.getParcelable(KEY_TOPIC)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false
    private var messageText: String = state?.getString(KEY_MESSAGE) ?: ""
    private var history: List<MessageEntity>? = state?.getParcelableArrayList(KEY_HISTORY)

    private val journal = HashSet<Int>()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: ChatView) {
        this.view = view

        view.setMessageText(messageText)

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe { loadTopic() }
        subscriptions += view.messageEditChanged().subscribe { messageText = it }
        subscriptions += view.sendClicks().subscribe {
            if (messageText.isNotBlank()) {
                sendMessage()
            }
        }
        subscriptions += view.msgReplyClicks().subscribe { message ->
            messageText = resourceProvider.replyFormText(message.text)
            view.setMessageText(messageText)
            view.requestFocus()
        }
        subscriptions += view.msgCopyClicks().subscribe { message ->
            view.copyToClipboard(message.text)
        }
        subscriptions += view.openProfileClicks().subscribe { message ->
            router?.openProfileScreen(message.userId)
        }
        subscriptions += view.msgReportClicks().subscribe { message ->
            reportMessage(message.msgId)
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

        subscriptions += eventsInteractor.subscribeOnEvents()
            .observeOn(schedulers.mainThread())
            .subscribe { response ->
                println("[polling] event received (chat)")
                response.messages?.let { messages ->
                    val countBefore = history?.size ?: 0
                    mergeHistory(messages.filter { it.topicId == topicId })
                    val countAfter = history?.size ?: 0

                    if (countAfter > countBefore) {
                        val items = convertHistory()

                        val dataSource = ListDataSource(items)
                        adapterPresenter.get().onDataSourceChanged(dataSource)

                        view.contentRangeInserted(0, 1)
                    }
                }
                response.deleted?.let { messages ->
                    val deletedIndexes = ArrayList<Int>()

                    messages
                        .filter { it.topicId == topicId }
                        .forEach { delMsg ->
                            history
                                ?.binarySearch { it.msgId.compareTo(delMsg.msgId) }
                                ?.takeIf { it >= 0 }
                                ?.let { deletedIndexes += it }
                        }

                    val mutableHistory = history?.toMutableList()
                    deletedIndexes.forEach { index ->
                        mutableHistory?.removeAt(index)
                    }
                    history = mutableHistory

                    val items = convertHistory()

                    val dataSource = ListDataSource(items)
                    adapterPresenter.get().onDataSourceChanged(dataSource)

                    deletedIndexes.forEach { index ->
                        view.contentItemRemoved(items.size - index)
                    }
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

    private fun sendMessage() {
        subscriptions += chatInteractor.sendMessage(topicId, messageText, null)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showSendProgress() }
            .doAfterTerminate { view?.showSendButton() }
            .subscribe(
                { onMessageSent() },
                { onMessageSendingError() }
            )
    }

    private fun onMessageSent() {
        messageText = ""
        view?.setMessageText(messageText)
    }

    private fun onMessageSendingError() {
        view?.showSendError()
    }

    private fun loadTopic() {
        subscriptions += chatInteractor.getTopic(topicId)
            .flatMap { topic ->
                this.topic = topic
                chatInteractor.loadHistory(topicId, 0, -1)
            }
            .map { mergeHistory(it) }
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

    private fun reportMessage(msgId: Int) {
        subscriptions += chatInteractor.reportMessage(msgId)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { view?.showReportSuccess() },
                { view?.showReportFailed() }
            )
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onItemClick(item: Item) {
        subscriptions += chatInteractor.getUserData()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { userData ->
                    val message = history?.findLast {
                        it.msgId == item.id.toInt()
                    } ?: return@subscribe
                    if (userData.role >= ROLE_ADMIN || userData.userId == message.userId) {
                        view?.showExtendedMessageDialog(message)
                    } else {
                        view?.showBaseMessageDialog(message)
                    }
                },
                {}
            )
    }

    override fun onLoadMore(msgId: Int) {
        if (history?.first()?.msgId == msgId && journal.add(msgId)) {
            subscriptions += chatInteractor.loadHistory(topicId, 0, msgId)
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
        mergeHistory(list)

        val items = convertHistory()

        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)

        view?.contentRangeInserted(countBefore, list.size)
        view?.showContent()
    }

    private fun onHistoryError() {
        view?.showContent()
    }

    private fun mergeHistory(list: List<MessageEntity>) {
        history = ((history ?: emptyList()) + list)
            .sortedBy { it.msgId }
            .distinctBy { it.msgId }
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
            .asReversed()
            .toList()
    }

}

private const val KEY_TOPIC = "topic"
private const val KEY_ERROR = "error"
private const val KEY_MESSAGE = "message"
private const val KEY_HISTORY = "history"
