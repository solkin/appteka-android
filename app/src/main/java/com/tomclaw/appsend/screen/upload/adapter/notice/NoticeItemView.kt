package com.tomclaw.appsend.screen.upload.adapter.notice

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface NoticeItemView : ItemView {
    fun setNoticeTypeInfo()
    fun setNoticeTypeWarning()
    fun setNoticeTypeError()
    fun setNoticeText(text: String)
    fun setOnClickListener(listener: (() -> Unit)?)
}

class NoticeItemViewHolder(view: View) : BaseViewHolder(view), NoticeItemView {

    private val background: View = view.findViewById(R.id.notice_back)
    private val icon: ImageView = view.findViewById(R.id.notice_icon)
    private val text: TextView = view.findViewById(R.id.notice_text)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setNoticeTypeInfo() {
        background.setBackgroundResource(R.drawable.bg_notice_info)
        icon.setImageResource(R.drawable.ic_info)
    }

    override fun setNoticeTypeWarning() {
        background.setBackgroundResource(R.drawable.bg_notice_warning)
        icon.setImageResource(R.drawable.ic_warning)
    }

    override fun setNoticeTypeError() {
        background.setBackgroundResource(R.drawable.bg_notice_error)
        icon.setImageResource(R.drawable.ic_error)
    }

    override fun setNoticeText(text: String) {
        this.text.bind(text)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        clickListener = null
    }
}