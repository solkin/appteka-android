package com.tomclaw.appsend.screen.chat

import android.net.Uri
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.chat.adapter.ItemListener
import com.tomclaw.appsend.screen.chat.adapter.incoming.IncomingMsgItem
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItem
import com.tomclaw.appsend.screen.chat.api.TranslationEntity
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.topics.COMMON_QNA_TOPIC_ICON
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.bananalytics.Bananalytics
import com.tomclaw.bananalytics.api.BreadcrumbCategory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.plusAssign

interface ChatPresenter : ItemListener {

    fun attachView(view: ChatView)

    fun detachView()

    fun attachRouter(router: ChatRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onAttachmentsPicked(uris: List<Uri>)

    interface ChatRouter {

        fun openProfileScreen(userId: Int)

        fun openAppScreen(packageName: String, title: String)

        fun openLoginScreen()

        fun openGallery(items: List<GalleryItem>, startIndex: Int)

        fun openImagePicker(remaining: Int)

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
    private var selectedAttachments: MutableList<Uri> =
        state?.getParcelableArrayListCompat(KEY_SELECTED, Uri::class.java)
            ?.toMutableList() ?: mutableListOf()
    private var history: List<MessageEntity>? =
        state?.getParcelableArrayListCompat(KEY_HISTORY, MessageEntity::class.java)
    private var translation: MutableMap<Int, TranslationEntity> =
        state?.getParcelableArrayListCompat(KEY_TRANSLATION, TranslationEntity::class.java)
            .orEmpty().associateBy { it.msgId }.toMutableMap()
    private var isTranslated: Boolean = state?.getBoolean(KEY_TRANSLATED, true) ?: true
    private var userBrief: UserBrief? =
        state?.getParcelableCompat(KEY_USER_BRIEF, UserBrief::class.java)

    private val journal = HashSet<Int>()

    private val subscriptions = CompositeDisposable()
    private var sendDisposable: Disposable? = null

    override fun attachView(view: ChatView) {
        this.view = view

        view.setMessageText(messageText)
        view.setSelectedAttachments(selectedAttachments)

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe { loadTopic() }
        subscriptions += view.messageEditChanged().subscribe { messageText = it }
        subscriptions += view.sendClicks().subscribe {
            if (messageText.isNotBlank() || selectedAttachments.isNotEmpty()) {
                sendMessage()
            }
        }
        subscriptions += view.cancelSendClicks().subscribe { cancelSend() }
        subscriptions += view.attachClicks().subscribe { remaining ->
            router?.openImagePicker(remaining)
        }
        subscriptions += view.attachmentRemoveClicks().subscribe { uri ->
            selectedAttachments.remove(uri)
            this.view?.setSelectedAttachments(selectedAttachments)
        }
        subscriptions += view.msgReplyClicks().subscribe { message ->
            replyToMessage(message)
        }
        subscriptions += view.msgReplySwipes().subscribe { msgId ->
            history?.findLast { it.msgId == msgId }?.let { replyToMessage(it) }
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
        subscriptions += view.msgDeleteClicks().subscribe { message ->
            deleteMessage(message.msgId)
        }
        subscriptions += view.pinChatClicks().subscribe {
            pinTopic()
        }
        subscriptions += view.chatTranslateClicks().subscribe {
            onGlobalTranslateToggled()
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
        adapterPresenter.get().onDataSourceChanged(items)
        return items
    }

    override fun detachView() {
        sendDisposable?.dispose()
        sendDisposable = null
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
        putString(KEY_MESSAGE, messageText)
        putParcelableArrayList(KEY_SELECTED, ArrayList(selectedAttachments))
        history?.let { putParcelableArrayList(KEY_HISTORY, ArrayList(it)) }
        putParcelableArrayList(KEY_TRANSLATION, ArrayList(translation.values))
        putBoolean(KEY_TRANSLATED, isTranslated)
        putParcelable(KEY_USER_BRIEF, userBrief)
    }

    override fun onAttachmentsPicked(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val remaining = MAX_ATTACHMENTS - selectedAttachments.size
        if (remaining <= 0) return
        val toAdd = uris.filterNot { it in selectedAttachments }.take(remaining)
        if (toAdd.isEmpty()) return
        selectedAttachments.addAll(toAdd)
        view?.setSelectedAttachments(selectedAttachments)
    }

    private fun sendMessage() {
        bananalytics.leaveBreadcrumb("Send message", BreadcrumbCategory.USER_ACTION)
        val attachmentsSnapshot = selectedAttachments.toList()
        val textSnapshot = messageText
        sendDisposable?.dispose()
        sendDisposable = chatInteractor.sendMessage(topicId, textSnapshot, attachmentsSnapshot)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showSendProgress() }
            .doAfterTerminate { view?.showSendButton() }
            .subscribe(
                { onMessageSent() },
                { onMessageSendingError(it, textSnapshot, attachmentsSnapshot) }
            )
    }

    private fun cancelSend() {
        sendDisposable?.dispose()
        sendDisposable = null
        view?.showSendButton()
    }

    private fun onMessageSent() {
        messageText = ""
        selectedAttachments.clear()
        view?.setMessageText(messageText)
        view?.setSelectedAttachments(selectedAttachments)

        reloadHistory()
    }

    private fun reloadHistory() {
        subscriptions += chatInteractor.loadHistory(topicId, 0, -1)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { messages ->
                    history = messages
                    applyInlineTranslations(messages)
                    bindHistory()
                    view?.contentUpdated()
                    view?.scrollBottom()
                    invalidateMenu()
                    readTopic()
                },
                { }
            )
    }

    private fun onMessageSendingError(
        ex: Throwable,
        text: String,
        attachments: List<Uri>,
    ) {
        bananalytics.leaveBreadcrumb("Message send error: ${ex.message}", BreadcrumbCategory.ERROR)
        bananalytics.trackException(ex, mapOf("action" to "send_message", "topicId" to topicId.toString()))
        ex.filterUnauthorizedErrors(
            authError = { view?.showUnauthorizedError() },
            other = {
                view?.showSendError {
                    messageText = text
                    selectedAttachments = attachments.toMutableList()
                    view?.setMessageText(text)
                    view?.setSelectedAttachments(selectedAttachments)
                    sendMessage()
                }
            }
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
        subscriptions += Observables
            .zip(
                chatInteractor.loadHistory(topicId, 0, -1),
                chatInteractor.getUserBrief()
            )
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                { (messages, briefWrapper) ->
                    userBrief = briefWrapper.userBrief
                    mergeHistory(messages)
                    onHistoryLoaded()
                },
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

    private fun deleteMessage(msgId: Int) {
        subscriptions += chatInteractor.reportMessage(msgId)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onMessageDeleted(msgId) },
                {
                    it.filterUnauthorizedErrors(
                        authError = { view?.showUnauthorizedError() },
                        other = { view?.showReportFailed() }
                    )
                }
            )
    }

    private fun onMessageDeleted(msgId: Int) {
        history = history?.filter { it.msgId != msgId }
        translation.remove(msgId)
        bindHistory()
        view?.contentUpdated()
    }

    private fun replyToMessage(message: MessageEntity) {
        val ownText = message.text.lines()
            .dropWhile { it.startsWith("> ") }
            .joinToString("\n")
            .trim()
        val quoted = ownText.lines().joinToString("\n") { "> $it" }
        val existing = messageText.lines()
            .dropWhile { it.startsWith("> ") || it.isBlank() }
            .joinToString("\n")
            .trim()
        messageText = if (existing.isNotBlank()) "$quoted\n\n$existing" else "$quoted\n"
        view?.setMessageText(messageText)
        view?.requestFocus()
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
            view?.showMenu(topic.isPinned, isTranslated)
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
        val canTranslate = message.type == 0 && message.text.isNotBlank()
        val userData = userBrief
        if (userData != null && (userData.role >= ROLE_ADMIN || userData.userId == message.userId)) {
            view?.showExtendedMessageDialog(message, translated, canTranslate)
        } else {
            view?.showBaseMessageDialog(message, translated, canTranslate)
        }
    }

    override fun onAttachmentClick(item: Item, index: Int) {
        val attachments = when (item) {
            is IncomingMsgItem -> item.attachments
            is OutgoingMsgItem -> item.attachments
            else -> null
        } ?: return
        val galleryItems = attachments.map { att ->
            GalleryItem(uri = android.net.Uri.parse(att.originalUrl), width = att.width, height = att.height)
        }
        router?.openGallery(galleryItems, index.coerceIn(0, galleryItems.size - 1))
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

        adapterPresenter.get().onDataSourceChanged(items)

        view?.contentRangeInserted(countBefore, list.size)
        view?.showContent()
    }

    private fun onHistoryError() {
        view?.showContent()
    }

    private fun mergeHistory(list: List<MessageEntity>) {
        applyInlineTranslations(list)
        history = ((history ?: emptyList()) + list)
            .sortedBy { it.msgId }
            .distinctBy { it.msgId }
    }

    private fun applyInlineTranslations(list: List<MessageEntity>) {
        list.forEach { msg ->
            val text = msg.translation
            if (!text.isNullOrBlank() && translation[msg.msgId] == null) {
                translation[msg.msgId] = TranslationEntity(
                    msgId = msg.msgId,
                    original = msg.text,
                    translation = text,
                    lang = msg.translationLang.orEmpty(),
                    translated = isTranslated,
                )
            }
        }
    }

    private fun onGlobalTranslateToggled() {
        isTranslated = !isTranslated
        translation.values.forEach { it.translated = isTranslated }
        bindHistory()
        view?.contentUpdated()
        invalidateMenu()
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
private const val KEY_SELECTED = "selected_attachments"
private const val KEY_HISTORY = "history"
private const val KEY_TRANSLATION = "translation"
private const val KEY_TRANSLATED = "translated"
private const val KEY_USER_BRIEF = "user_brief"

private const val ROLE_ADMIN = 200
private const val MAX_ATTACHMENTS = 5
