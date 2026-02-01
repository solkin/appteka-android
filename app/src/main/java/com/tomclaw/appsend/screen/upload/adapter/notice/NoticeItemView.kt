package com.tomclaw.appsend.screen.upload.adapter.notice

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface NoticeItemView : ItemView {

    fun setNoticeTypeInfo()

    fun setNoticeTypeWarning()

    fun setNoticeTypeError()

    fun setNoticeText(text: String)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class NoticeItemViewHolder(view: View) : BaseItemViewHolder(view), NoticeItemView {

    private val context = view.context
    private val background: View = view.findViewById(R.id.notice_back)
    private val icon: ImageView = view.findViewById(R.id.notice_icon)
    private val text: TextView = view.findViewById(R.id.notice_text)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setNoticeTypeInfo() {
        setBackgroundColor(R.color.block_info_back_color)
        icon.setImageResource(R.drawable.ic_info)
        icon.setColorFilter(ContextCompat.getColor(context, R.color.block_info_color))
        text.setTextColor(ContextCompat.getColor(context, R.color.block_info_text_color))
    }

    override fun setNoticeTypeWarning() {
        setBackgroundColor(R.color.block_warning_back_color)
        icon.setImageResource(R.drawable.ic_warning)
        icon.setColorFilter(ContextCompat.getColor(context, R.color.block_warning_color))
        text.setTextColor(ContextCompat.getColor(context, R.color.block_warning_text_color))
    }

    override fun setNoticeTypeError() {
        setBackgroundColor(R.color.block_error_back_color)
        icon.setImageResource(R.drawable.ic_error)
        icon.setColorFilter(ContextCompat.getColor(context, R.color.block_error_color))
        text.setTextColor(ContextCompat.getColor(context, R.color.block_error_text_color))
    }

    override fun setNoticeText(text: String) {
        this.text.bind(text)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

    private fun setBackgroundColor(colorRes: Int) {
        val backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
        background.backgroundTintList = backgroundTintList
        background.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
    }

}
