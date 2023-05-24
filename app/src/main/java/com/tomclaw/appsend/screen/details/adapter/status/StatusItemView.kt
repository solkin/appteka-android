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

    fun setStatusTypeInfo()

    fun setStatusTypeWarning()

    fun setStatusTypeError()

    fun setStatusText(text: String)

}

@Suppress("DEPRECATION")
class StatusItemViewHolder(view: View) : BaseViewHolder(view), StatusItemView {

    private val context = view.context
    private val resources = view.resources
    private val background: View = view.findViewById(R.id.status_back)
    private val icon: ImageView = view.findViewById(R.id.status_icon)
    private val text: TextView = view.findViewById(R.id.status_text)

    override fun setStatusTypeInfo() {
        setBackgroundColor(R.color.block_info_back_color)
        icon.setImageResource(R.drawable.ic_info)
        icon.setColorFilter(resources.getColor(R.color.block_info_color))
        text.setTextColor(resources.getColor(R.color.block_info_text_color))
    }

    override fun setStatusTypeWarning() {
        setBackgroundColor(R.color.block_warning_back_color)
        icon.setImageResource(R.drawable.ic_warning)
        icon.setColorFilter(resources.getColor(R.color.block_warning_color))
        text.setTextColor(resources.getColor(R.color.block_warning_text_color))
    }

    override fun setStatusTypeError() {
        setBackgroundColor(R.color.block_error_back_color)
        icon.setImageResource(R.drawable.ic_error)
        icon.setColorFilter(resources.getColor(R.color.block_error_color))
        text.setTextColor(resources.getColor(R.color.block_error_text_color))
    }

    override fun setStatusText(text: String) {
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
