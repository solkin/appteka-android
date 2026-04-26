package com.tomclaw.appsend.screen.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityHintResolver
import com.tomclaw.appsend.core.permissions.CapabilityResult
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.screen.chat.view.ChatAttachmentsStrip
import com.tomclaw.appsend.uikit.permissions.PermissionBanner
import com.tomclaw.appsend.util.ActionItem
import com.tomclaw.appsend.util.ActionsAdapter
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.imageloader.util.fetch
import io.reactivex.rxjava3.core.Observable

interface ChatView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setSubtitle(subtitle: String)

    fun setMessageText(messageText: String)

    fun requestFocus()

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun contentRangeInserted(position: Int, count: Int)

    fun contentItemRemoved(position: Int)

    fun scrollBottom()

    fun showError()

    fun showSendButton()

    fun showSendProgress()

    fun showSendError(onRetry: () -> Unit)

    fun showUnauthorizedError()

    /**
     * Surface a capability-denied response (server rejected the
     * action because of an ACL/ownership/role check). Routed through
     * the same hint resolver as proactive UI.
     */
    fun showCapabilityDenied(capability: Capability)

    /**
     * Apply the send-message capability. Allowed → composer fully enabled
     * and banner hidden. Denied/Unknown denial → composer disabled and
     * banner shown with the explanation provided by the server.
     */
    fun setSendEnabled(result: CapabilityResult)

    fun setSelectedAttachments(uris: List<Uri>)

    fun getSelectedAttachments(): List<Uri>

    fun copyToClipboard(text: String)

    fun showBaseMessageDialog(message: MessageEntity, translated: Boolean, canTranslate: Boolean)

    fun showExtendedMessageDialog(message: MessageEntity, translated: Boolean, canTranslate: Boolean)

    fun showReportSuccess()

    fun showReportFailed()

    fun showTranslationFailed()

    fun showMenu(hasPin: Boolean, translated: Boolean)

    fun hideMenu()

    fun navigationClicks(): Observable<Unit>

    fun toolbarClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun messageEditChanged(): Observable<String>

    fun sendClicks(): Observable<Unit>

    fun cancelSendClicks(): Observable<Unit>

    fun attachClicks(): Observable<Int>

    fun attachmentRemoveClicks(): Observable<Uri>

    fun msgReplyClicks(): Observable<MessageEntity>

    fun msgReplySwipes(): Observable<Int>

    fun msgCopyClicks(): Observable<MessageEntity>

    fun msgTranslateClicks(): Observable<MessageEntity>

    fun openProfileClicks(): Observable<MessageEntity>

    fun msgReportClicks(): Observable<MessageEntity>

    fun msgDeleteClicks(): Observable<MessageEntity>

    fun pinChatClicks(): Observable<Unit>

    fun chatTranslateClicks(): Observable<Unit>

    fun loginClicks(): Observable<Unit>

}

class ChatViewImpl(
    private val view: View,
    private val preferences: ChatPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter,
    adapterPresenter: AdapterPresenter,
) : ChatView {

    private val context = view.context
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val back: View = view.findViewById(R.id.go_back)
    private val icon: ImageView = view.findViewById(R.id.icon)
    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val header: View = view.findViewById(R.id.header)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val errorText: TextView = view.findViewById(R.id.error_text)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val messageEdit: EditText = view.findViewById(R.id.message_edit)
    private val attachButton: MaterialButton = view.findViewById(R.id.attach_button)
    private val sendButton: MaterialButton = view.findViewById(R.id.send_button)
    private val sendProgress: CircularProgressIndicator = view.findViewById(R.id.send_progress)
    private val attachmentsStrip: ChatAttachmentsStrip = view.findViewById(R.id.attachments_strip)
    private val permissionBanner: PermissionBanner = view.findViewById(R.id.permission_banner)

    private var isSending = false

    private val navigationRelay = PublishRelay.create<Unit>()
    private val toolbarRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val messageEditRelay = PublishRelay.create<String>()
    private val sendRelay = PublishRelay.create<Unit>()
    private val cancelSendRelay = PublishRelay.create<Unit>()
    private val attachRelay = PublishRelay.create<Int>()
    private val msgReplyRelay = PublishRelay.create<MessageEntity>()
    private val msgReplySwipesRelay = PublishRelay.create<Int>()
    private val msgCopyRelay = PublishRelay.create<MessageEntity>()
    private val msgTranslateRelay = PublishRelay.create<MessageEntity>()
    private val openProfileRelay = PublishRelay.create<MessageEntity>()
    private val msgReportRelay = PublishRelay.create<MessageEntity>()
    private val msgDeleteRelay = PublishRelay.create<MessageEntity>()
    private val pinChatRelay = PublishRelay.create<Unit>()
    private val chatTranslateRelay = PublishRelay.create<Unit>()
    private val loginRelay = PublishRelay.create<Unit>()

    private val layoutManager: LinearLayoutManager

    init {
        title.setText(R.string.chat_activity)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.pin, R.id.pin_off -> {
                    pinChatRelay.accept(Unit)
                    true
                }

                R.id.translate, R.id.translate_off -> {
                    chatTranslateRelay.accept(Unit)
                    true
                }

                else -> false
            }
        }
        back.clicks(navigationRelay)
        header.setOnClickListener { toolbarRelay.accept(Unit) }

        val orientation = RecyclerView.VERTICAL
        layoutManager = LinearLayoutManager(view.context, orientation, true)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        val swipeCallback = SwipeToReplyCallback(context, adapterPresenter) { msgId ->
            msgReplySwipesRelay.accept(msgId)
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recycler)

        retryButton.clicks(retryRelay)
        messageEdit.addTextChangedListener { text ->
            messageEditRelay.accept(text.toString())
        }
        sendButton.setOnClickListener {
            if (isSending) cancelSendRelay.accept(Unit) else sendRelay.accept(Unit)
        }
        attachButton.setOnClickListener {
            val remaining = ChatAttachmentsStrip.DEFAULT_MAX_TILES -
                attachmentsStrip.getUris().size
            if (remaining > 0) attachRelay.accept(remaining)
        }
        attachmentsStrip.addTileClicks().subscribe {
            val remaining = ChatAttachmentsStrip.DEFAULT_MAX_TILES -
                attachmentsStrip.getUris().size
            if (remaining > 0) attachRelay.accept(remaining)
        }
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
            centerCrop()
            placeholder(drawableRes = R.drawable.app_placeholder)
            onLoading { imageView ->
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageResource(R.drawable.app_placeholder)
            }
        }
    }

    override fun setTitle(title: String) {
        this.title.text = title
    }

    override fun setSubtitle(subtitle: String) {
        this.subtitle.text = subtitle
    }

    override fun setMessageText(messageText: String) {
        messageEdit.setText(messageText, TextView.BufferType.EDITABLE)
    }

    override fun requestFocus() {
        messageEdit.setSelection(messageEdit.length())
        messageEdit.requestFocus()
    }

    override fun showProgress() {
        flipper.displayedChild = 0
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        flipper.displayedChild = 0
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError() {
        flipper.displayedChild = 1
        errorText.setText(R.string.chat_loading_error)
    }

    override fun showSendButton() {
        isSending = false
        sendButton.setIconResource(R.drawable.ic_send)
        sendProgress.visibility = View.GONE
        attachButton.isEnabled = true
        messageEdit.isEnabled = true
    }

    override fun showSendProgress() {
        isSending = true
        sendButton.setIconResource(R.drawable.ic_close)
        sendProgress.visibility = View.VISIBLE
        attachButton.isEnabled = false
        messageEdit.isEnabled = false
    }

    override fun showSendError(onRetry: () -> Unit) {
        Snackbar.make(recycler, R.string.error_sending_message, Snackbar.LENGTH_LONG)
            .setAction(R.string.retry) { onRetry() }
            .show()
    }

    override fun showUnauthorizedError() {
        Snackbar
            .make(recycler, R.string.authorization_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.login_button) {
                loginRelay.accept(Unit)
            }
            .show()
    }

    override fun showCapabilityDenied(capability: Capability) {
        val text = CapabilityHintResolver(recycler.resources).resolveText(capability)
        Snackbar.make(recycler, text, Snackbar.LENGTH_LONG).show()
    }

    override fun setSendEnabled(result: CapabilityResult) {
        when (result) {
            CapabilityResult.Allowed, CapabilityResult.Unknown -> {
                permissionBanner.hide()
                messageEdit.isEnabled = true
                attachButton.isEnabled = true
                sendButton.isEnabled = true
            }
            is CapabilityResult.Denied -> {
                permissionBanner.showFor(result.capability)
                messageEdit.isEnabled = false
                attachButton.isEnabled = false
                sendButton.isEnabled = false
            }
        }
    }

    override fun setSelectedAttachments(uris: List<Uri>) {
        attachmentsStrip.setMaxTiles(ChatAttachmentsStrip.DEFAULT_MAX_TILES)
        attachmentsStrip.setUris(uris)
    }

    override fun getSelectedAttachments(): List<Uri> = attachmentsStrip.getUris()

    override fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("", text))
    }

    override fun showBaseMessageDialog(
        message: MessageEntity,
        translated: Boolean,
        canTranslate: Boolean
    ) {
        showMessageDialog(message, translated, canTranslate, extended = false)
    }

    override fun showExtendedMessageDialog(
        message: MessageEntity,
        translated: Boolean,
        canTranslate: Boolean
    ) {
        showMessageDialog(message, translated, canTranslate, extended = true)
    }

    private fun showMessageDialog(
        message: MessageEntity,
        translated: Boolean,
        canTranslate: Boolean,
        extended: Boolean,
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = mutableListOf<ActionItem>()
        actions.add(ActionItem(MENU_REPLY, context.getString(R.string.reply), R.drawable.ic_reply))
        actions.add(
            ActionItem(
                MENU_COPY,
                context.getString(R.string.copy),
                R.drawable.ic_content_copy
            )
        )
        if (canTranslate) {
            if (translated) {
                actions.add(
                    ActionItem(
                        MENU_TRANSLATE,
                        context.getString(R.string.original),
                        R.drawable.ic_translate_off
                    )
                )
            } else {
                actions.add(
                    ActionItem(
                        MENU_TRANSLATE,
                        context.getString(R.string.translate),
                        R.drawable.ic_translate
                    )
                )
            }
        }
        actions.add(
            ActionItem(
                MENU_PROFILE,
                context.getString(R.string.profile),
                R.drawable.ic_account
            )
        )
        if (extended) {
            actions.add(
                ActionItem(
                    MENU_DELETE,
                    context.getString(R.string.delete),
                    R.drawable.ic_delete
                )
            )
        } else {
            actions.add(
                ActionItem(
                    MENU_REPORT,
                    context.getString(R.string.report),
                    R.drawable.ic_alert
                )
            )
        }

        val actionsAdapter = ActionsAdapter(actions) { actionId ->
            bottomSheetDialog.dismiss()
            when (actionId) {
                MENU_REPLY -> msgReplyRelay.accept(message)
                MENU_COPY -> msgCopyRelay.accept(message)
                MENU_TRANSLATE -> msgTranslateRelay.accept(message)
                MENU_PROFILE -> openProfileRelay.accept(message)
                MENU_REPORT -> msgReportRelay.accept(message)
                MENU_DELETE -> msgDeleteRelay.accept(message)
            }
        }

        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = actionsAdapter

        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

    override fun showReportSuccess() {
        Snackbar.make(recycler, R.string.message_report_sent, Snackbar.LENGTH_LONG).show()
    }

    override fun showReportFailed() {
        Snackbar.make(recycler, R.string.error_message_report, Snackbar.LENGTH_LONG).show()
    }

    override fun showTranslationFailed() {
        Snackbar.make(recycler, R.string.translation_error, Snackbar.LENGTH_LONG).show()
    }

    override fun showMenu(hasPin: Boolean, translated: Boolean) {
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.chat_menu)
        if (hasPin) {
            toolbar.menu.removeItem(R.id.pin)
        } else {
            toolbar.menu.removeItem(R.id.pin_off)
        }
        if (translated) {
            toolbar.menu.removeItem(R.id.translate)
        } else {
            toolbar.menu.removeItem(R.id.translate_off)
        }
        toolbar.invalidateMenu()
    }

    override fun hideMenu() {
        toolbar.menu.clear()
        toolbar.invalidateMenu()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun contentUpdated(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun contentRangeInserted(position: Int, count: Int) {
        adapter.notifyItemRangeInserted(position, count)
        val visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (visiblePosition == 0) {
            recycler.scrollToPosition(position)
        }
    }

    override fun contentItemRemoved(position: Int) {
        adapter.notifyItemRemoved(position)
    }

    override fun scrollBottom() {
        recycler.scrollToPosition(0)
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun toolbarClicks(): Observable<Unit> = toolbarRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun messageEditChanged(): Observable<String> = messageEditRelay

    override fun sendClicks(): Observable<Unit> = sendRelay

    override fun cancelSendClicks(): Observable<Unit> = cancelSendRelay

    override fun attachClicks(): Observable<Int> = attachRelay

    override fun attachmentRemoveClicks(): Observable<Uri> = attachmentsStrip.removeTileClicks()

    override fun msgReplyClicks(): Observable<MessageEntity> = msgReplyRelay

    override fun msgReplySwipes(): Observable<Int> = msgReplySwipesRelay

    override fun msgCopyClicks(): Observable<MessageEntity> = msgCopyRelay

    override fun msgTranslateClicks(): Observable<MessageEntity> = msgTranslateRelay

    override fun openProfileClicks(): Observable<MessageEntity> = openProfileRelay

    override fun msgReportClicks(): Observable<MessageEntity> = msgReportRelay

    override fun msgDeleteClicks(): Observable<MessageEntity> = msgDeleteRelay

    override fun pinChatClicks(): Observable<Unit> = pinChatRelay

    override fun chatTranslateClicks(): Observable<Unit> = chatTranslateRelay

    override fun loginClicks(): Observable<Unit> = loginRelay

}

private const val MENU_REPLY = 1
private const val MENU_COPY = 2
private const val MENU_TRANSLATE = 3
private const val MENU_PROFILE = 4
private const val MENU_REPORT = 5
private const val MENU_DELETE = 6
private const val DURATION_MEDIUM = 300L
