package com.tomclaw.appsend.screen.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.util.ActionItem
import com.tomclaw.appsend.util.ActionsAdapter
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder
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

    fun showSendError()

    fun showUnauthorizedError()

    fun copyToClipboard(text: String)

    fun showBaseMessageDialog(message: MessageEntity, translated: Boolean)

    fun showExtendedMessageDialog(message: MessageEntity, translated: Boolean)

    fun showReportSuccess()

    fun showReportFailed()

    fun showTranslationFailed()

    fun showMenu(hasPin: Boolean)

    fun hideMenu()

    fun navigationClicks(): Observable<Unit>

    fun toolbarClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun messageEditChanged(): Observable<String>

    fun sendClicks(): Observable<Unit>

    fun msgReplyClicks(): Observable<MessageEntity>

    fun msgCopyClicks(): Observable<MessageEntity>

    fun msgTranslateClicks(): Observable<MessageEntity>

    fun openProfileClicks(): Observable<MessageEntity>

    fun msgReportClicks(): Observable<MessageEntity>

    fun pinChatClicks(): Observable<Unit>

    fun loginClicks(): Observable<Unit>

}

class ChatViewImpl(
    private val view: View,
    private val preferences: ChatPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
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
    private val sendButton: MaterialButton = view.findViewById(R.id.send_button)
    private val sendProgress: CircularProgressIndicator = view.findViewById(R.id.send_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val toolbarRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val messageEditRelay = PublishRelay.create<String>()
    private val sendRelay = PublishRelay.create<Unit>()
    private val msgReplyRelay = PublishRelay.create<MessageEntity>()
    private val msgCopyRelay = PublishRelay.create<MessageEntity>()
    private val msgTranslateRelay = PublishRelay.create<MessageEntity>()
    private val openProfileRelay = PublishRelay.create<MessageEntity>()
    private val msgReportRelay = PublishRelay.create<MessageEntity>()
    private val pinChatRelay = PublishRelay.create<Unit>()
    private val loginRelay = PublishRelay.create<Unit>()

    private val layoutManager: LinearLayoutManager

    init {
        title.setText(R.string.chat_activity)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.pin -> {
                    pinChatRelay.accept(Unit)
                    true
                }

                R.id.pin_off -> {
                    pinChatRelay.accept(Unit)
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

        retryButton.clicks(retryRelay)
        messageEdit.addTextChangedListener { text ->
            messageEditRelay.accept(text.toString())
        }
        sendButton.clicks(sendRelay)
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
            centerCrop()
            withPlaceholder(R.drawable.app_placeholder)
            placeholder = {
                with(it.get()) {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.app_placeholder)
                }
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
        sendButton.visibility = View.VISIBLE
        sendProgress.visibility = View.GONE
    }

    override fun showSendProgress() {
        sendButton.visibility = View.GONE
        sendProgress.visibility = View.VISIBLE
    }

    override fun showSendError() {
        Snackbar.make(recycler, R.string.error_sending_message, Snackbar.LENGTH_LONG).show()
    }

    override fun showUnauthorizedError() {
        Snackbar
            .make(recycler, R.string.authorization_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.login_button) {
                loginRelay.accept(Unit)
            }
            .show()
    }

    override fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("", text))
    }

    override fun showBaseMessageDialog(message: MessageEntity, translated: Boolean) {
        showMessageDialog(message, translated, extended = false)
    }

    override fun showExtendedMessageDialog(message: MessageEntity, translated: Boolean) {
        showMessageDialog(message, translated, extended = true)
    }

    private fun showMessageDialog(
        message: MessageEntity,
        translated: Boolean,
        extended: Boolean,
    ) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = sheetView.findViewById(R.id.actions_recycler)

        val actions = mutableListOf<ActionItem>()
        actions.add(ActionItem(MENU_REPLY, context.getString(R.string.reply), R.drawable.ic_reply))
        actions.add(ActionItem(MENU_COPY, context.getString(R.string.copy), R.drawable.ic_content_copy))
        if (translated) {
            actions.add(ActionItem(MENU_TRANSLATE, context.getString(R.string.original), R.drawable.ic_translate_off))
        } else {
            actions.add(ActionItem(MENU_TRANSLATE, context.getString(R.string.translate), R.drawable.ic_translate))
        }
        actions.add(ActionItem(MENU_PROFILE, context.getString(R.string.profile), R.drawable.ic_account))
        if (extended) {
            actions.add(ActionItem(MENU_REPORT, context.getString(R.string.delete), R.drawable.ic_delete))
        } else {
            actions.add(ActionItem(MENU_REPORT, context.getString(R.string.report), R.drawable.ic_alert))
        }

        val actionsAdapter = ActionsAdapter(actions) { actionId ->
            bottomSheetDialog.dismiss()
            when (actionId) {
                MENU_REPLY -> msgReplyRelay.accept(message)
                MENU_COPY -> msgCopyRelay.accept(message)
                MENU_TRANSLATE -> msgTranslateRelay.accept(message)
                MENU_PROFILE -> openProfileRelay.accept(message)
                MENU_REPORT -> msgReportRelay.accept(message)
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

    override fun showMenu(hasPin: Boolean) {
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.chat_menu)
        if (hasPin) {
            toolbar.menu.removeItem(R.id.pin)
        } else {
            toolbar.menu.removeItem(R.id.pin_off)
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

    override fun msgReplyClicks(): Observable<MessageEntity> = msgReplyRelay

    override fun msgCopyClicks(): Observable<MessageEntity> = msgCopyRelay

    override fun msgTranslateClicks(): Observable<MessageEntity> = msgTranslateRelay

    override fun openProfileClicks(): Observable<MessageEntity> = openProfileRelay

    override fun msgReportClicks(): Observable<MessageEntity> = msgReportRelay

    override fun pinChatClicks(): Observable<Unit> = pinChatRelay

    override fun loginClicks(): Observable<Unit> = loginRelay

}

private const val MENU_REPLY = 1
private const val MENU_COPY = 2
private const val MENU_TRANSLATE = 3
private const val MENU_PROFILE = 4
private const val MENU_REPORT = 5
private const val DURATION_MEDIUM = 300L
