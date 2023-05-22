package com.tomclaw.appsend.screen.details.adapter.status

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface StatusItemView : ItemView {

    fun setNoticeTypeInfo()

    fun setNoticeTypeWarning()

    fun setNoticeTypeError()

    fun setNoticeText(text: String)

}

@Suppress("DEPRECATION")
class StatusItemViewHolder(view: View) : BaseViewHolder(view), StatusItemView {

    private val context = view.context
    private val resources = view.resources
    private val background: View = view.findViewById(R.id.notice_back)
    private val icon: ImageView = view.findViewById(R.id.notice_icon)
    private val text: TextView = view.findViewById(R.id.notice_text)

    override fun setNoticeTypeInfo() {
        setBackgroundColor(R.color.notice_info_back_color)
        icon.setImageResource(R.drawable.ic_info)
        icon.setColorFilter(resources.getColor(R.color.notice_info_color))
        text.setTextColor(resources.getColor(R.color.notice_info_text_color))
    }

    override fun setNoticeTypeWarning() {
        setBackgroundColor(R.color.notice_warning_back_color)
        icon.setImageResource(R.drawable.ic_warning)
        icon.setColorFilter(resources.getColor(R.color.notice_warning_color))
        text.setTextColor(resources.getColor(R.color.notice_warning_text_color))
    }

    override fun setNoticeTypeError() {
        setBackgroundColor(R.color.notice_error_back_color)
        icon.setImageResource(R.drawable.ic_error)
        icon.setColorFilter(resources.getColor(R.color.notice_error_color))
        text.setTextColor(resources.getColor(R.color.notice_error_text_color))
    }

    override fun setNoticeText(text: String) {
        this.text.bind(text)
    }

    override fun onUnbind() {
    }

    private fun setBackgroundColor(colorRes: Int) {
        val backgroundTintList = ColorStateList.valueOf(resources.getColor(colorRes))
        background.backgroundTintList = backgroundTintList
        background.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
    }

}
