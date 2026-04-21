package com.tomclaw.appsend.screen.chat.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.imageloader.util.fetch
import io.reactivex.rxjava3.core.Observable

class ChatAttachmentsStrip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private val container: LinearLayout
    private val tileSize = dp(64)
    private val tileRadius = dp(12).toFloat()
    private val gap = dp(8)
    private val addTileRelay = PublishRelay.create<Unit>()
    private val removeRelay = PublishRelay.create<Uri>()

    private var maxTiles: Int = DEFAULT_MAX_TILES
    private var uris: List<Uri> = emptyList()

    init {
        isHorizontalScrollBarEnabled = false
        container = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        addView(container)
        setUris(emptyList())
    }

    fun setMaxTiles(value: Int) {
        maxTiles = value
    }

    fun setUris(list: List<Uri>) {
        uris = list
        rebuild()
        visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
    }

    fun addTileClicks(): Observable<Unit> = addTileRelay

    fun removeTileClicks(): Observable<Uri> = removeRelay

    fun addUri(uri: Uri) {
        if (uri in uris) return
        if (uris.size >= maxTiles) return
        setUris(uris + uri)
    }

    fun addUris(list: List<Uri>) {
        if (list.isEmpty()) return
        val combined = (uris + list).distinct().take(maxTiles)
        setUris(combined)
    }

    fun removeUri(uri: Uri) {
        setUris(uris.filter { it != uri })
    }

    fun clear() {
        setUris(emptyList())
    }

    fun getUris(): List<Uri> = uris

    private fun rebuild() {
        container.removeAllViews()
        uris.forEachIndexed { index, uri ->
            container.addView(createTile(uri), tileLayoutParams(isFirst = index == 0))
        }
        if (uris.size < maxTiles) {
            container.addView(createAddTile(), tileLayoutParams(isFirst = uris.isEmpty()))
        }
    }

    private fun tileLayoutParams(isFirst: Boolean) = LinearLayout.LayoutParams(tileSize, tileSize)
        .apply { if (!isFirst) leftMargin = gap }

    private fun createTile(uri: Uri): FrameLayout {
        val card = MaterialCardView(context).apply {
            radius = tileRadius
            cardElevation = 0f
        }
        val image = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        image.fetch(uri) {
            centerCrop()
            placeholder(drawableRes = R.drawable.app_placeholder)
        }
        card.addView(image)

        val remove = ImageView(context).apply {
            setImageResource(R.drawable.ic_close)
            imageTintList = ColorStateList.valueOf(Color.WHITE)
            setBackgroundResource(R.drawable.chat_attachment_remove_bg)
            val size = dp(20)
            layoutParams = FrameLayout.LayoutParams(size, size, Gravity.TOP or Gravity.END)
            val pad = dp(3)
            setPadding(pad, pad, pad, pad)
            setOnClickListener { removeRelay.accept(uri) }
        }

        val wrapper = FrameLayout(context)
        wrapper.addView(card, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        wrapper.addView(remove)
        return wrapper
    }

    private fun createAddTile(): MaterialCardView {
        val card = MaterialCardView(context).apply {
            radius = tileRadius
            cardElevation = 0f
            strokeWidth = dp(1)
            setOnClickListener { addTileRelay.accept(Unit) }
        }
        val image = ImageView(context).apply {
            setImageResource(R.drawable.ic_image_plus)
            imageTintList = ColorStateList.valueOf(
                resolveAttrColor(com.google.android.material.R.attr.colorOnSurfaceVariant)
            )
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        card.addView(image)
        return card
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun resolveAttrColor(attrRes: Int): Int {
        val typed = TypedValue()
        context.theme.resolveAttribute(attrRes, typed, true)
        return typed.data
    }

    companion object {
        const val DEFAULT_MAX_TILES = 5
    }
}
