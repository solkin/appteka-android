package com.tomclaw.appsend.screen.details.adapter.status

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.button.MaterialButton
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface StatusItemView : ItemView {

    fun setStatusTypeInfo()

    fun setStatusTypeWarning()

    fun setStatusTypeError()

    fun hideActionButton()

    fun showActionButton(label: String)

    fun setStatusText(text: String)

    fun setOnActionClickListener(listener: (() -> Unit)?)

}

class StatusItemViewHolder(view: View) : BaseViewHolder(view), StatusItemView {

    private val context = view.context
    private val background: View = view.findViewById(R.id.status_back)
    private val icon: ImageView = view.findViewById(R.id.status_icon)
    private val text: TextView = view.findViewById(R.id.status_text)
    private val actionButton: MaterialButton = view.findViewById(R.id.action_button)

    private var actionClickListener: (() -> Unit)? = null

    init {
        actionButton.setOnClickListener { actionClickListener?.invoke() }
    }

    override fun setStatusTypeInfo() {
        setBackgroundColor(R.color.block_info_back_color)
        icon.setImageResource(R.drawable.ic_info)
        icon.setColorFilter(ContextCompat.getColor(context, R.color.block_info_color))
        text.setTextColor(ContextCompat.getColor(context, R.color.block_info_text_color))
        actionButton.setRippleColorResource(R.color.block_info_color)
        actionButton.setTextColor(ContextCompat.getColor(context, R.color.block_info_text_color))
    }

    override fun setStatusTypeWarning() {
        setBackgroundColor(R.color.block_warning_back_color)
        icon.setImageResource(R.drawable.ic_warning)
        icon.setColorFilter(ContextCompat.getColor(context, R.color.block_warning_color))
        text.setTextColor(ContextCompat.getColor(context, R.color.block_warning_text_color))
        actionButton.setRippleColorResource(R.color.block_warning_color)
        actionButton.setTextColor(ContextCompat.getColor(context, R.color.block_warning_text_color))
    }

    override fun setStatusTypeError() {
        setBackgroundColor(R.color.block_error_back_color)
        icon.setImageResource(R.drawable.ic_error)
        icon.setColorFilter(ContextCompat.getColor(context, R.color.block_error_color))
        text.setTextColor(ContextCompat.getColor(context, R.color.block_error_text_color))
        actionButton.setRippleColorResource(R.color.block_error_color)
        actionButton.setTextColor(ContextCompat.getColor(context, R.color.block_error_text_color))
    }

    override fun hideActionButton() {
        actionButton.hide()
    }

    override fun showActionButton(label: String) {
        actionButton.text = label
        actionButton.show()
    }

    override fun setStatusText(text: String) {
        this.text.bind(text)
    }

    private fun setBackgroundColor(colorRes: Int) {
        val backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, colorRes))
        background.backgroundTintList = backgroundTintList
        background.backgroundTintMode = PorterDuff.Mode.SRC_ATOP
    }

    override fun setOnActionClickListener(listener: (() -> Unit)?) {
        this.actionClickListener = listener
    }

    override fun onUnbind() {
        this.actionClickListener = null
    }

}
