package com.tomclaw.appsend.screen.chat.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import com.tomclaw.imageloader.util.fetch

class MessageAttachmentsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val gapPx: Int = dp(2)
    private val cornerPx: Float = dp(12).toFloat()
    private val maxBubbleWidthPx: Int = dp(240)
    private val rowHeightPx: Int = dp(110)

    init {
        orientation = VERTICAL
    }

    fun setAttachments(
        attachments: List<MsgAttachment>,
        onClick: (index: Int) -> Unit = {}
    ) {
        removeAllViews()
        if (attachments.isEmpty()) return
        val items = attachments.take(MAX_TILES)
        when (items.size) {
            1 -> addSingle(items[0], onClick)
            2 -> addRow(items, 0, 2, onClick)
            3 -> addRow(items, 0, 3, onClick)
            4 -> {
                addRow(items, 0, 2, onClick)
                addRow(items, 2, 2, onClick)
            }
            else -> {
                addRow(items, 0, 2, onClick)
                addRow(items, 2, 3, onClick)
            }
        }
    }

    private fun addSingle(attachment: MsgAttachment, onClick: (Int) -> Unit) {
        val aspect = computeAspect(attachment.width, attachment.height)
        val targetWidth = maxBubbleWidthPx
        val targetHeight = (targetWidth / aspect).toInt()
        addView(
            createTile(attachment.previewUrl, targetWidth, targetHeight) { onClick(0) },
            LayoutParams(targetWidth, targetHeight)
        )
    }

    private fun addRow(
        items: List<MsgAttachment>,
        offset: Int,
        count: Int,
        onClick: (Int) -> Unit
    ) {
        val isSecondRow = childCount > 0
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(maxBubbleWidthPx, rowHeightPx).apply {
                if (isSecondRow) topMargin = gapPx
            }
        }
        for (i in 0 until count) {
            val index = offset + i
            if (index >= items.size) break
            val tile = createTile(items[index].previewUrl, 0, rowHeightPx) { onClick(index) }
            val lp = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            if (i > 0) lp.leftMargin = gapPx
            row.addView(tile, lp)
        }
        addView(row)
    }

    private fun createTile(
        previewUrl: String,
        width: Int,
        height: Int,
        onClick: () -> Unit
    ): MaterialCardView {
        val card = MaterialCardView(context).apply {
            radius = cornerPx
            cardElevation = 0f
            setCardBackgroundColor(resolveAttrColor(com.google.android.material.R.attr.colorSurfaceContainerHighest))
            isClickable = true
            isFocusable = true
            foreground = null
            setOnClickListener { onClick() }
        }
        val imageView = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        imageView.fetch(previewUrl) {
            centerCrop()
            placeholder(drawableRes = R.drawable.ic_chat_image_placeholder)
        }
        card.addView(imageView)
        return card
    }

    private fun computeAspect(width: Int, height: Int): Float {
        if (width <= 0 || height <= 0) return 1f
        val raw = width.toFloat() / height.toFloat()
        return raw.coerceIn(MIN_ASPECT, MAX_ASPECT)
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun resolveAttrColor(attrRes: Int): Int {
        val typed = TypedValue()
        context.theme.resolveAttribute(attrRes, typed, true)
        return typed.data
    }

    companion object {
        private const val MAX_TILES = 5
        private const val MIN_ASPECT = 0.75f // 3:4 portrait
        private const val MAX_ASPECT = 4f / 3f // 4:3 landscape
    }
}
