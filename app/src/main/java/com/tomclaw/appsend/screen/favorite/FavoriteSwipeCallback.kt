package com.tomclaw.appsend.screen.favorite

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R as MaterialR
import com.tomclaw.appsend.R

class FavoriteSwipeCallback(
    context: Context,
    private val onSwiped: (itemId: Long) -> Unit,
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.START or ItemTouchHelper.END,
) {

    private val background: Drawable
    private val icon: Drawable
    private val textPaint: Paint
    private val label: String
    private val density: Float = context.resources.displayMetrics.density
    private val horizontalPadding: Int = (density * HORIZONTAL_PADDING_DP).toInt()
    private val iconLabelGap: Float = density * ICON_LABEL_GAP_DP

    init {
        val bgColor = context.resolveThemeColor(MaterialR.attr.colorErrorContainer)
        val onBgColor = context.resolveThemeColor(MaterialR.attr.colorOnErrorContainer)
        background = bgColor.toDrawable()
        icon = requireNotNull(ContextCompat.getDrawable(context, R.drawable.ic_delete))
            .mutate()
            .also { DrawableCompat.setTint(it, onBgColor) }
        textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = onBgColor
            textSize = density * LABEL_TEXT_SP
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        label = context.getString(R.string.unmark_favorite)
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float =
        defaultValue * SWIPE_ESCAPE_MULTIPLIER

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float =
        SWIPE_THRESHOLD_FRACTION

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onSwiped(viewHolder.itemId)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean,
    ) {
        val itemView = viewHolder.itemView
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX != 0f) {
            background.setBounds(
                itemView.left,
                itemView.top,
                itemView.right,
                itemView.bottom,
            )
            background.draw(c)

            val iconSize = (density * ICON_SIZE_DP).toInt()
            val iconTop = itemView.top + (itemView.height - iconSize) / 2
            val iconBottom = iconTop + iconSize
            val textBaseline = itemView.top + itemView.height / 2f -
                (textPaint.descent() + textPaint.ascent()) / 2f
            val labelWidth = textPaint.measureText(label)

            val isRtl = recyclerView.layoutDirection == View.LAYOUT_DIRECTION_RTL
            val revealOnRight = if (isRtl) dX > 0 else dX < 0
            val revealWidth = kotlin.math.abs(dX).toInt()
            val fullyRevealed =
                revealWidth >= iconSize + horizontalPadding * 2 + labelWidth + iconLabelGap

            if (revealOnRight) {
                val iconRight = itemView.right - horizontalPadding
                val iconLeft = iconRight - iconSize
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)
                if (fullyRevealed) {
                    c.drawText(
                        label,
                        iconLeft - iconLabelGap - labelWidth,
                        textBaseline,
                        textPaint,
                    )
                }
            } else {
                val iconLeft = itemView.left + horizontalPadding
                val iconRight = iconLeft + iconSize
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)
                if (fullyRevealed) {
                    c.drawText(
                        label,
                        iconRight + iconLabelGap,
                        textBaseline,
                        textPaint,
                    )
                }
            }
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

}

private fun Context.resolveThemeColor(@AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

private const val HORIZONTAL_PADDING_DP = 24
private const val ICON_LABEL_GAP_DP = 16f
private const val ICON_SIZE_DP = 24
private const val LABEL_TEXT_SP = 14f
private const val SWIPE_THRESHOLD_FRACTION = 0.45f
private const val SWIPE_ESCAPE_MULTIPLIER = 1.2f
