package com.tomclaw.appsend.screen.chat

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.chat.adapter.ItemListener
import com.tomclaw.appsend.screen.chat.api.MsgTranslateResponse
import com.tomclaw.appsend.screen.chat.api.TranslationEntity
import com.tomclaw.appsend.screen.topics.COMMON_QNA_TOPIC_ICON
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.bananalytics.Bananalytics
import com.tomclaw.bananalytics.api.BreadcrumbCategory
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

        fun openAppScreen(packageName: String, title: String)

        fun openLoginScreen()

        fun leaveScreen()

    }

}

class ChatPresenterImpl(
    topicEntity: TopicEntity?,
    private val topicId: Int,
    private val bananalytics: Bananalytics,
    private val converter: MessageConverter,
    private val chatInteractor: ChatInteractor,
    private val resourceProvider: ChatResourceProvider,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ChatPresenter {

    private var view: ChatView? = null
    private var router: ChatPresenter.ChatRouter? = null

    private var topic: TopicEntity? =
        state?.getParcelableCompat(KEY_TOPIC, TopicEntity::class.java) ?: topicEntity
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true
    private var messageText: String = state?.getString(KEY_MESSAGE).orEmpty()
    private var history: List<MessageEntity>? =
        state?.getParcelableArrayListCompat(KEY_HISTORY, MessageEntity::class.java)
    private var translation: MutableMap<Int, TranslationEntity> =
        state?.getParcelableArrayListCompat(KEY_TRANSLATION, TranslationEntity::class.java)
            .orEmpty().associateBy { it.msgId }.toMutableMap()

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
        subscriptions += view.msgTranslateClicks().subscribe { message ->
            translateMessage(message)
        }
        subscriptions += view.openProfileClicks().subscribe { message ->
            router?.openProfileScreen(message.userId)
        }
        subscriptions += view.msgReportClicks().subscribe { message ->
            reportMessage(message.msgId)
        }
        subscriptions += view.pinChatClicks().subscribe {
            pinTopic()
        }
        subscriptions += view.toolbarClicks().subscribe {
            val packageName = topic?.packageName
            if (!packageName.isNullOrEmpty()) {
                router?.openAppScreen(packageName, topic?.title.orEmpty())
            }
        }
        subscriptions += view.loginClicks().subscribe {
            router?.openLoginScreen()
        }

        when {
            isError -> {
                onTopicError()
            }

            topic != null -> {
                onTopicLoaded()
                if (history != null) {
                    onHistoryLoaded()
                } else {
                    loadHistory()
                }
            }

            else -> {
                loadTopic()
            }
        }

    }

    private fun bindHistory(): List<Item> {
        val items = convertHistory()
        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        return items
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
        putParcelableArrayList(KEY_TRANSLATION, ArrayList(translation.values))
    }

    private fun sendMessage() {
        bananalytics.leaveBreadcrumb("Send message", BreadcrumbCategory.USER_ACTION)
        subscriptions += chatInteractor.sendMessage(topicId, messageText, null)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showSendProgress() }
            .doAfterTerminate { view?.showSendButton() }
            .subscribe(
                { onMessageSent() },
                { onMessageSendingError(it) }
            )
    }

    private fun onMessageSent() {
        messageText = ""
        view?.setMessageText(messageText)

        reloadHistory()
    }

    private fun reloadHistory() {
        subscriptions += chatInteractor.loadHistory(topicId, 0, -1)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { messages ->
                    history = messages
                    bindHistory()
                    view?.contentUpdated()
                    view?.scrollBottom()
                    invalidateMenu()
                    readTopic()
                },
                { }
            )
    }

    private fun onMessageSendingError(ex: Throwable) {
        bananalytics.leaveBreadcrumb("Message send error: ${ex.message}", BreadcrumbCategory.ERROR)
        bananalytics.trackException(ex, mapOf("action" to "send_message", "topicId" to topicId.toString()))
        ex.filterUnauthorizedErrors(
            authError = { view?.showUnauthorizedError() },
            other = { view?.showSendError() }
        )
    }

    private fun loadTopic() {
        subscriptions += chatInteractor.getTopic(topicId)
            .map { this.topic = it }
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                {
                    onTopicLoaded()
                    loadHistory()
                },
                { onTopicError() }
            )
    }

    private fun onTopicLoaded() {
        val topic = topic ?: return
        val icon = when (topic.topicId) {
            1 -> COMMON_QNA_TOPIC_ICON
            else -> topic.icon.orEmpty()
        }
        val title = when (topic.topicId) {
            1 -> resourceProvider.commonQuestionsTopicTitle()
            else -> topic.title
        }
        val description = when (topic.topicId) {
            1 -> resourceProvider.commonQuestionsTopicDescription()
            else -> topic.description
        }
        isError = false
        view?.setIcon(icon)
        view?.setTitle(title)
        view?.setSubtitle(description ?: topic.packageName.orEmpty())
    }

    private fun loadHistory() {
        subscriptions += chatInteractor.loadHistory(topicId, 0, -1)
            .map { mergeHistory(it) }
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                { onHistoryLoaded() },
                { onTopicError() }
            )
    }

    private fun onHistoryLoaded() {
        bindHistory()

        view?.contentUpdated()
        view?.showContent()
        invalidateMenu()

        readTopic()
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
                {
                    it.filterUnauthorizedErrors(
                        authError = { view?.showUnauthorizedError() },
                        other = { view?.showReportFailed() }
                    )
                }
            )
    }

    private fun translateMessage(msg: MessageEntity) {
        translation[msg.msgId]?.let {
            it.translated = !it.translated
            onMessageTranslated()
        } ?: run {
            subscriptions += chatInteractor.translateMessage(msg.msgId)
                .observeOn(schedulers.mainThread())
                .doOnSubscribe { view?.showProgress() }
                .doAfterTerminate { view?.showContent() }
                .subscribe(
                    {
                        translation[msg.msgId] = TranslationEntity(
                            msgId = msg.msgId,
                            original = msg.text,
                            translation = it.text,
                            lang = it.lang,
                            translated = true,
                        )
                        onMessageTranslated()
                    },
                    {
                        it.filterUnauthorizedErrors(
                            authError = { view?.showUnauthorizedError() },
                            other = { view?.showTranslationFailed() }
                        )
                    }
                )
        }
    }

    private fun onMessageTranslated() {
        bindHistory()

        view?.contentUpdated()
        view?.showContent()
    }

    private fun readTopic() {
        if (topic?.isPinned == true) {
            history?.takeIf { it.isNotEmpty() }?.last()?.let { msg ->
                if (msg.msgId > (topic?.readMsgId ?: 0)) {
                    subscriptions += chatInteractor.readTopic(topicId, msg.msgId)
                        .observeOn(schedulers.mainThread())
                        .subscribe({ }, { })
                }
            }
        }
    }

    private fun pinTopic() {
        subscriptions += chatInteractor.pinTopic(topicId)
            .flatMap { chatInteractor.getTopic(topicId) }
            .observeOn(schedulers.mainThread())
            .subscribe(
                { newTopic ->
                    topic = newTopic
                    invalidateMenu()
                },
                { it.filterUnauthorizedErrors({ view?.showUnauthorizedError() }, {}) }
            )
    }

    private fun invalidateMenu() {
        val topic = topic ?: return
        if (history?.isNotEmpty() == true) {
            view?.showMenu(topic.isPinned)
        } else {
            view?.hideMenu()
        }
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onItemClick(item: Item) {
        val message = history?.findLast {
            it.msgId == item.id.toInt()
        } ?: return
        val translated = translation[message.msgId]?.translated == true
        subscriptions += chatInteractor.getUserBrief()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { userData ->
                    if (userData.role >= ROLE_ADMIN || userData.userId == message.userId) {
                        view?.showExtendedMessageDialog(message, translated)
                    } else {
                        view?.showBaseMessageDialog(message, translated)
                    }
                },
                { view?.showBaseMessageDialog(message, translated) }
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
                val item = converter.convert(it, prevMsg, translation[it.msgId])
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
private const val KEY_TRANSLATION = "translation"

private const val ROLE_ADMIN = 200
