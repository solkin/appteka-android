package com.tomclaw.appsend.screen.chat

import android.annotation.SuppressLint
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
import com.tomclaw.appsend.util.ColorHelper.getAttributedColor
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface ChatView {

    fun setTitle(title: String)

    fun setMessageText(messageText: String)

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun contentRangeInserted(position: Int, count: Int)

    fun showError()

    fun showSendButton()

    fun showSendProgress()

    fun showSendError()

    fun showBaseMessageDialog()

    fun showExtendedMessageDialog()

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun messageEditChanged(): Observable<String>

    fun sendClicks(): Observable<Unit>

    fun msgMenuClicks(): Observable<Int>

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
    private val msgMenuRelay = PublishRelay.create<Int>()

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

    override fun showBaseMessageDialog() {
        showMessageDialog(R.menu.msg_base_menu)
    }

    override fun showExtendedMessageDialog() {
        showMessageDialog(R.menu.msg_extended_menu)
    }

    private fun showMessageDialog(@MenuRes menuId: Int) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(view.context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(context, R.attr.text_primary_color))
            .setMenu(menuId)
            .setItemClickListener {
                msgMenuRelay.accept(it.itemId)
            }
            .createDialog()
            .show()
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

    override fun msgMenuClicks(): Observable<Int> = msgMenuRelay

}

private const val DURATION_MEDIUM = 300L

const val ACTION_REPLY = 1
const val ACTION_COPY = 2
const val ACTION_OPEN_PROFILE = 3
const val ACTION_REPORT = 4
const val ACTION_DELETE = 5
