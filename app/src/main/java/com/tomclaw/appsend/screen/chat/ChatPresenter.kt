package com.tomclaw.appsend.screen.chat

import android.net.Uri
import android.os.Bundle
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityPolicy
import com.tomclaw.appsend.core.permissions.CapabilityResult
import com.tomclaw.appsend.core.permissions.UserCapabilitiesProvider
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.chat.adapter.ItemListener
import com.tomclaw.appsend.screen.chat.adapter.incoming.IncomingMsgItem
import com.tomclaw.appsend.screen.chat.adapter.loadmore.LOAD_MORE_ITEM_ID
import com.tomclaw.appsend.screen.chat.adapter.loadmore.LoadMoreItem
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItem
import com.tomclaw.appsend.screen.chat.api.TranslationEntity
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.topics.COMMON_QNA_TOPIC_ICON
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterCapabilityErrors
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.stripLeadingQuote
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
    private val capabilitiesProvider: UserCapabilitiesProvider,
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
    private var hasMoreHistory: Boolean = state?.getBoolean(KEY_HAS_MORE_HISTORY, true) ?: true

    private val journal = HashSet<Int>()

    // Composer state. The view stays dumb; this trio is the single
    // source of truth driving applyComposerState() into atomic view
    // setters.
    private var sendInProgress: Boolean = false
    private var sendDeniedCapability: Capability? = null
    private var attachDeniedCapability: Capability? = null

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
            val deniedCap = attachDeniedCapability
            if (deniedCap != null) {
                this.view?.showCapabilityDenied(deniedCap)
            } else {
                router?.openImagePicker(remaining)
            }
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
            router?.openProfileScreen(message.author.id)
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

        // Image attach is opt-in via CHAT_IMAGE_ATTACH (global, not
        // per-topic). Pull the snapshot first, then keep listening so
        // the gate updates if capabilities arrive late or are
        // refreshed mid-session.
        refreshAttachCapability()
        subscriptions += capabilitiesProvider.observeCapabilities()
            .observeOn(schedulers.mainThread())
            .subscribe { refreshAttachCapability() }

        applyComposerState()

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

    private fun refreshAttachCapability() {
        val result = CapabilityPolicy.check(
            action = CapabilityAction.CHAT_IMAGE_ATTACH,
            capabilities = capabilitiesProvider.getCapabilities(),
        )
        attachDeniedCapability = (result as? CapabilityResult.Denied)?.capability
        applyComposerState()
    }

    // Composer state is the cross product of (sending in progress) ×
    // (send capability) × (attach capability). The presenter owns the
    // composition; the view receives only atomic enable/visible/mute
    // commands, so re-renders cannot clobber each other across
    // independent state changes.
    private fun applyComposerState() {
        val view = view ?: return

        val sendDenied = sendDeniedCapability
        if (sendDenied != null) {
            view.setSendBanner(sendDenied)
            view.setComposerInputEnabled(false)
            view.setSendButtonEnabled(false)
            view.setAttachButtonEnabled(false)
            view.setAttachButtonMuted(false)
            view.setAttachButtonVisible(true)
            return
        }
        view.setSendBanner(null)
        view.setSendButtonEnabled(true)

        if (sendInProgress) {
            view.setComposerInputEnabled(false)
            view.setAttachButtonEnabled(false)
            view.setAttachButtonMuted(false)
            view.setAttachButtonVisible(true)
            return
        }
        view.setComposerInputEnabled(true)

        val attachDenied = attachDeniedCapability
        if (attachDenied != null) {
            // Mechanics intact — clickable + visually muted; attachClicks
            // would surface the hint via showCapabilityDenied. Hidden
            // for now per product, flip visibility to surface.
            view.setAttachButtonEnabled(true)
            view.setAttachButtonMuted(true)
            view.setAttachButtonVisible(false)
        } else {
            view.setAttachButtonEnabled(true)
            view.setAttachButtonMuted(false)
            view.setAttachButtonVisible(true)
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
        putBoolean(KEY_HAS_MORE_HISTORY, hasMoreHistory)
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
            .doOnSubscribe { setSendInProgress(true) }
            .doAfterTerminate { setSendInProgress(false) }
            .subscribe(
                { onMessageSent() },
                { onMessageSendingError(it, textSnapshot, attachmentsSnapshot) }
            )
    }

    private fun cancelSend() {
        sendDisposable?.dispose()
        sendDisposable = null
        setSendInProgress(false)
    }

    private fun setSendInProgress(inProgress: Boolean) {
        sendInProgress = inProgress
        view?.setSendInProgress(inProgress)
        applyComposerState()
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
        ex.filterCapabilityErrors(
            authError = { view?.showUnauthorizedError() },
            capabilityDenied = { cap ->
                messageText = text
                selectedAttachments = attachments.toMutableList()
                view?.setMessageText(text)
                view?.setSelectedAttachments(selectedAttachments)
                view?.showCapabilityDenied(cap)
            },
            other = {
                view?.showSendError {
                    messageText = text
                    selectedAttachments = attachments.toMutableList()
                    view?.setMessageText(text)
                    view?.setSelectedAttachments(selectedAttachments)
                    sendMessage()
                }
            },
        )
    }

    private fun loadTopic() {
        subscriptions += chatInteractor.getTopic(topicId)
            .flatMap { newTopic ->
                Observables
                    .zip(
                        chatInteractor.loadHistory(topicId, 0, -1),
                        chatInteractor.getUserBrief()
                    )
                    .map { (messages, brief) -> Triple(newTopic, messages, brief) }
            }
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                { (newTopic, messages, briefWrapper) ->
                    topic = newTopic
                    onTopicLoaded()
                    userBrief = briefWrapper.userBrief
                    mergeHistory(messages)
                    onHistoryLoaded()
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
        // Reflect the send-message capability in the composer. When the
        // viewer is read-only (or anonymous), the composer is disabled
        // and a banner explains why.
        val sendCheck = CapabilityPolicy.check(
            action = CapabilityAction.CHAT_MESSAGE_SEND,
            capabilities = topic.capabilities,
        )
        sendDeniedCapability = (sendCheck as? CapabilityResult.Denied)?.capability
        applyComposerState()
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
        val ownText = message.text.stripLeadingQuote()
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
        // Server-side capability is the source of truth for whether this
        // viewer may delete this specific message. Fallback to the
        // legacy role/ownership heuristic when the field is missing so
        // that fresh clients talking to old servers still behave sanely.
        val canDelete = CapabilityPolicy.isAllowed(
            action = CapabilityAction.CHAT_MESSAGE_DELETE,
            capabilities = message.capabilities,
            allowOnUnknown = legacyCanDelete(message),
        )
        if (canDelete) {
            view?.showExtendedMessageDialog(message, translated, canTranslate)
        } else {
            view?.showBaseMessageDialog(message, translated, canTranslate)
        }
    }

    private fun legacyCanDelete(message: MessageEntity): Boolean {
        val userData = userBrief ?: return false
        return userData.role >= ROLE_ADMIN || userData.id == message.author.id
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
                .doAfterTerminate { journal.remove(msgId) }
                .subscribe(
                    { onHistoryPageLoaded(it) },
                    { }
                )
        }
    }

    private fun onHistoryPageLoaded(list: List<MessageEntity>) {
        if (list.isEmpty()) {
            hasMoreHistory = false
        } else {
            mergeHistory(list)
        }
        bindHistory()
        view?.contentUpdated()
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
        if (history.isEmpty()) return emptyList()
        var prevMsg: MessageEntity? = null
        val items: MutableList<Item> = history
            .map {
                val item = converter.convert(it, prevMsg, translation[it.msgId])
                prevMsg = it
                item
            }
            .asReversed()
            .toMutableList()
        if (hasMoreHistory) {
            items.add(LoadMoreItem(id = LOAD_MORE_ITEM_ID, msgId = history.first().msgId))
        }
        return items
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
private const val KEY_HAS_MORE_HISTORY = "has_more_history"

private const val ROLE_ADMIN = 200
private const val MAX_ATTACHMENTS = 5
