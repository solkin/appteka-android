package com.tomclaw.appsend.screen.chat

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.chat.adapter.incoming.IncomingMsgItem
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItem
import com.tomclaw.appsend.util.adapter.AdapterPresenter

class SwipeToReplyCallback(
    context: Context,
    private val adapterPresenter: AdapterPresenter,
    private val onReply: (msgId: Int) -> Unit,
) : ItemTouchHelper.Callback() {

    private val density = context.resources.displayMetrics.density
    private val threshold = density * THRESHOLD_DP
    private val maxDrag = threshold * MAX_DRAG_FACTOR
    private val iconPadding = density * ICON_PADDING_DP
    private val replyIcon: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.ic_reply)?.mutate()?.apply {
            setTint(resolveTint(context))
        }

    private var pastThreshold = false
    private var hapticTriggered = false

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
    ): Int {
        return if (viewHolder.isSwipeable()) {
            makeMovementFlags(0, ItemTouchHelper.LEFT)
        } else 0
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float = Float.MAX_VALUE

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float = Float.MAX_VALUE

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 2f

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        if (actionState != ItemTouchHelper.ACTION_STATE_SWIPE) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }
        val limitedDX = dX.coerceIn(-maxDrag, 0f)
        super.onChildDraw(c, recyclerView, viewHolder, limitedDX, dY, actionState, isCurrentlyActive)
        drawReplyIcon(c, viewHolder.itemView, limitedDX)

        if (isCurrentlyActive) {
            val reached = limitedDX <= -threshold
            if (reached && !hapticTriggered) {
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                hapticTriggered = true
            } else if (!reached) {
                hapticTriggered = false
            }
            pastThreshold = reached
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (pastThreshold) {
            viewHolder.msgId()?.let(onReply)
        }
        pastThreshold = false
        hapticTriggered = false
    }

    private fun drawReplyIcon(canvas: Canvas, itemView: View, dX: Float) {
        val icon = replyIcon ?: return
        val progress = (-dX / threshold).coerceIn(0f, 1f)
        val iconSize = icon.intrinsicWidth
        val left = (itemView.right - iconPadding - iconSize).toInt()
        val top = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        icon.setBounds(left, top, left + iconSize, top + icon.intrinsicHeight)
        icon.alpha = (progress * 255).toInt()
        icon.draw(canvas)
    }

    private fun RecyclerView.ViewHolder.isSwipeable(): Boolean = msgId() != null

    private fun RecyclerView.ViewHolder.msgId(): Int? {
        val pos = bindingAdapterPosition
        if (pos == RecyclerView.NO_POSITION) return null
        return when (val item = adapterPresenter.getItem(pos)) {
            is IncomingMsgItem -> item.takeIf { it.type == 0 }?.msgId
            is OutgoingMsgItem -> item.takeIf { it.type == 0 }?.msgId
            else -> null
        }
    }

}

private const val THRESHOLD_DP = 72f
private const val MAX_DRAG_FACTOR = 1.2f
private const val ICON_PADDING_DP = 16f

private fun resolveTint(context: Context): Int {
    val typed = context.obtainStyledAttributes(intArrayOf(android.R.attr.textColorSecondary))
    val color = typed.getColor(0, 0)
    typed.recycle()
    return color
}
