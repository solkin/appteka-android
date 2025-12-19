package com.tomclaw.appsend.screen.topics

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

// TopicsPreferencesProvider is expected to be defined externally in its own file.
// We only keep the interface reference in the constructor.

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
    // Dependency kept for code completion, but its methods are no longer used for styling.
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
        viewFlipper.displayedChild = CHILD_INTRO
    }

    override fun showProgress() {
        viewFlipper.displayedChild = CHILD_CONTENT
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        viewFlipper.displayedChild = CHILD_CONTENT
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError() {
        viewFlipper.displayedChild = CHILD_ERROR
    }

    /**
     * Shows topic pin/unpin options using BottomSheetDialog with default system styling.
     * All custom theme/color application logic has been removed.
     */
    override fun showMessageDialog(topicId: Int, isPinned: Boolean) {
        // Use default system theme for BottomSheetDialog
        val dialog = BottomSheetDialog(context)
        
        // Inflate the custom layout for bottom sheet actions/list 
        val actionView = View.inflate(context, R.layout.bottom_sheet_actions, null)
        val actionsRecycler: RecyclerView = actionView.findViewById(R.id.actions_recycler)

        val pinTitle = if (isPinned) R.string.unpin else R.string.pin
        val pinIcon = if (isPinned) R.drawable.ic_pin_off else R.drawable.ic_pin
        
        // Setup the list of actions (only Pin/Unpin action)
        val actions = listOf(
            ActionItem(MENU_PIN, context.getString(pinTitle), pinIcon)
        )

        // Create adapter and handle click listener
        val actionsAdapter = ActionsAdapter(actions) { itemId ->
            dialog.dismiss()
            if (itemId == MENU_PIN) {
                pinTopicRelay.accept(topicId)
            }
        }
        
        actionsRecycler.layoutManager = LinearLayoutManager(context)
        actionsRecycler.adapter = actionsAdapter

        dialog.setContentView(actionView)
        dialog.show()
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
private const val CHILD_INTRO = 0
private const val CHILD_CONTENT = 1
private const val CHILD_ERROR = 2

// -------------------------------------------------------------------------------------------------
// Helper classes for Material BottomSheetDialog 
// -------------------------------------------------------------------------------------------------

// Data class to represent a single action item in the bottom sheet
data class ActionItem(
    val id: Int,
    val title: String, 
    val iconRes: Int
)

class ActionsAdapter(
    private val actions: List<ActionItem>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ActionsAdapter.ActionViewHolder>() {

    class ActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.action_title)
        val icon: ImageView = view.findViewById(R.id.action_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        // R.layout.item_bottom_sheet_action is assumed to exist
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bottom_sheet_action, parent, false)
        return ActionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        val item = actions[position]
        holder.title.text = item.title
        holder.icon.setImageResource(item.iconRes)
        holder.itemView.setOnClickListener {
            onClick(item.id)
        }
    }

    override fun getItemCount() = actions.size
}