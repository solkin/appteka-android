package com.tomclaw.appsend.screen.chat

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewFlipper
import android.widget.ViewSwitcher
import androidx.annotation.MenuRes
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.util.*
import com.tomclaw.appsend.util.ColorHelper.getAttributedColor
import io.reactivex.rxjava3.core.Observable

interface ChatView {

    fun setTitle(title: String)

    fun setMessageText(messageText: String)

    fun requestFocus()

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun contentRangeInserted(position: Int, count: Int)

    fun showError()

    fun showSendButton()

    fun showSendProgress()

    fun showSendError()

    fun copyToClipboard(text: String)

    fun showBaseMessageDialog(message: MessageEntity)

    fun showExtendedMessageDialog(message: MessageEntity)

    fun showReportSuccess()

    fun showReportFailed()

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun messageEditChanged(): Observable<String>

    fun sendClicks(): Observable<Unit>

    fun msgReplyClicks(): Observable<MessageEntity>

    fun msgCopyClicks(): Observable<MessageEntity>

    fun openProfileClicks(): Observable<MessageEntity>

    fun msgReportClicks(): Observable<MessageEntity>

}

class ChatViewImpl(
    private val view: View,
    private val preferences: ChatPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : ChatView {

    private val context = view.context
    private val flipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val errorText: TextView = view.findViewById(R.id.error_text)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val messageEdit: EditText = view.findViewById(R.id.message_edit)
    private val sendSwitcher: ViewSwitcher = view.findViewById(R.id.send_switcher)
    private val sendButton: View = view.findViewById(R.id.send_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val messageEditRelay = PublishRelay.create<String>()
    private val sendRelay = PublishRelay.create<Unit>()
    private val msgReplyRelay = PublishRelay.create<MessageEntity>()
    private val msgCopyRelay = PublishRelay.create<MessageEntity>()
    private val openProfileRelay = PublishRelay.create<MessageEntity>()
    private val msgReportRelay = PublishRelay.create<MessageEntity>()

    private val layoutManager: LinearLayoutManager

    init {
        toolbar.setTitle(R.string.chat_activity)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

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

    override fun setTitle(title: String) {
        toolbar.title = title
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
        sendSwitcher.displayedChild = 0
    }

    override fun showSendProgress() {
        sendSwitcher.displayedChild = 1
    }

    override fun showSendError() {
        Snackbar.make(recycler, R.string.error_sending_message, Snackbar.LENGTH_LONG).show()
    }

    override fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("", text))
    }

    override fun showBaseMessageDialog(message: MessageEntity) {
        showMessageDialog(message, R.menu.msg_base_menu)
    }

    override fun showExtendedMessageDialog(message: MessageEntity) {
        showMessageDialog(message, R.menu.msg_extended_menu)
    }

    private fun showMessageDialog(message: MessageEntity, @MenuRes menuId: Int) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(view.context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(context, R.attr.text_primary_color))
            .setMenu(menuId)
            .setItemClickListener {
                when (it.itemId) {
                    R.id.menu_reply -> msgReplyRelay.accept(message)
                    R.id.menu_copy -> msgCopyRelay.accept(message)
                    R.id.menu_profile -> openProfileRelay.accept(message)
                    R.id.menu_report -> msgReportRelay.accept(message)
                }
            }
            .createDialog()
            .show()
    }

    override fun showReportSuccess() {
        Snackbar.make(recycler, R.string.message_report_sent, Snackbar.LENGTH_LONG).show()
    }

    override fun showReportFailed() {
        Snackbar.make(recycler, R.string.error_message_report, Snackbar.LENGTH_LONG).show()
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

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun messageEditChanged(): Observable<String> = messageEditRelay

    override fun sendClicks(): Observable<Unit> = sendRelay

    override fun msgReplyClicks(): Observable<MessageEntity> = msgReplyRelay

    override fun msgCopyClicks(): Observable<MessageEntity> = msgCopyRelay

    override fun openProfileClicks(): Observable<MessageEntity> = openProfileRelay

    override fun msgReportClicks(): Observable<MessageEntity> = msgReportRelay

}

private const val DURATION_MEDIUM = 300L
