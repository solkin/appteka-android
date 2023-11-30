package com.tomclaw.appsend.screen.topics

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface TopicsView {

    fun showIntro()

    fun showProgress()

    fun showContent()

    fun showError()

    fun showMessageDialog(topicId: Int, isPinned: Boolean)

    fun showPinFailed()

    fun showUnauthorizedError()

    fun getStartedClicks(): Observable<Unit>

    fun retryButtonClicks(): Observable<Unit>

    fun pinTopicClicks(): Observable<Int>

    fun loginClicks(): Observable<Unit>

    fun contentUpdated()

    fun contentUpdated(position: Int)

}

class TopicsViewImpl(
    private val view: View,
    private val preferences: TopicsPreferencesProvider,
    private val adapter: SimpleRecyclerAdapter
) : TopicsView {

    private val context = view.context
    private val coordinator: CoordinatorLayout = view.findViewById(R.id.coordinator)
    private val viewFlipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val getStartedButton: View = view.findViewById(R.id.get_started_button)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val errorText: TextView = view.findViewById(R.id.error_text)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)

    private val getStartedRelay = PublishRelay.create<Unit>()
    private val retryButtonRelay = PublishRelay.create<Unit>()
    private val pinTopicRelay = PublishRelay.create<Int>()
    private val loginRelay = PublishRelay.create<Unit>()

    init {
        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        getStartedButton.setOnClickListener { getStartedRelay.accept(Unit) }
        retryButton.setOnClickListener { retryButtonRelay.accept(Unit) }
        errorText.setText(R.string.topics_loading_failed)
    }

    override fun showIntro() {
        viewFlipper.displayedChild = 0
    }

    override fun showProgress() {
        viewFlipper.displayedChild = 1
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        viewFlipper.displayedChild = 1
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError() {
        viewFlipper.displayedChild = 2
    }

    override fun showMessageDialog(topicId: Int, isPinned: Boolean) {
        val theme = R.style.BottomSheetDialogDark.takeIf { preferences.isDarkTheme() }
            ?: R.style.BottomSheetDialogLight
        BottomSheetBuilder(view.context, theme)
            .setMode(BottomSheetBuilder.MODE_LIST)
            .setIconTintColor(getAttributedColor(context, R.attr.menu_icons_tint))
            .setItemTextColor(getAttributedColor(context, R.attr.text_primary_color))
            .apply {
                if (isPinned) {
                    addItem(
                        MENU_PIN,
                        R.string.unpin,
                        R.drawable.ic_pin_off
                    ).setItemClickListener { pinTopicRelay.accept(topicId) }
                } else {
                    addItem(
                        MENU_PIN,
                        R.string.pin,
                        R.drawable.ic_pin
                    ).setItemClickListener { pinTopicRelay.accept(topicId) }
                }
            }
            .createDialog()
            .show()
    }

    override fun showPinFailed() {
        Snackbar.make(coordinator, R.string.error_topic_pin, Snackbar.LENGTH_LONG).show()
    }

    override fun showUnauthorizedError() {
        Snackbar
            .make(coordinator, R.string.authorization_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.login_button) {
                loginRelay.accept(Unit)
            }
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun contentUpdated(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun getStartedClicks(): Observable<Unit> = getStartedRelay

    override fun retryButtonClicks(): Observable<Unit> = retryButtonRelay

    override fun pinTopicClicks(): Observable<Int> = pinTopicRelay

    override fun loginClicks(): Observable<Unit> = loginRelay

}

private const val DURATION_MEDIUM = 300L
private const val MENU_PIN = 1
