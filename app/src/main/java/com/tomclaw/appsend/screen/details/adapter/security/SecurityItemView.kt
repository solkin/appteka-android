package com.tomclaw.appsend.screen.details.adapter.security

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface SecurityItemView : ItemView {

    fun setSecurityTypeNotScanned()

    fun setSecurityTypeScanning()

    fun setSecurityTypeSafe()

    fun setSecurityTypeSuspicious()

    fun setSecurityTypeMalware()

    fun setSecurityTypeUnknown()

    fun hideActionButton()

    fun showActionButton(label: String)

    fun setSecurityText(text: String)

    fun setOnActionClickListener(listener: (() -> Unit)?)

}

@Suppress("DEPRECATION")
class SecurityItemViewHolder(view: View) : BaseViewHolder(view), SecurityItemView {

    private val resources = view.resources
    private val background: View = view.findViewById(R.id.security_back)
    private val icon: ImageView = view.findViewById(R.id.security_icon)
    private val progress: CircularProgressIndicator = view.findViewById(R.id.security_progress)
    private val text: TextView = view.findViewById(R.id.security_text)
    private val actionButton: MaterialButton = view.findViewById(R.id.action_button)

    private var actionClickListener: (() -> Unit)? = null

    init {
        actionButton.setOnClickListener { actionClickListener?.invoke() }
    }

    override fun setSecurityTypeNotScanned() {
        setBackgroundColor(R.color.block_info_back_color)
        icon.setImageResource(R.drawable.ic_security)
        icon.setColorFilter(resources.getColor(R.color.block_info_color))
        icon.show()
        progress.hide()
        text.setTextColor(resources.getColor(R.color.block_info_text_color))
        actionButton.setRippleColorResource(R.color.block_info_color)
        actionButton.setTextColor(resources.getColor(R.color.block_info_text_color))
    }

    override fun setSecurityTypeScanning() {
        setBackgroundColor(R.color.block_info_back_color)
        icon.hide()
        progress.show()
        text.setTextColor(resources.getColor(R.color.block_info_text_color))
        actionButton.setRippleColorResource(R.color.block_info_color)
        actionButton.setTextColor(resources.getColor(R.color.block_info_text_color))
    }

    override fun setSecurityTypeSafe() {
        setBackgroundColor(R.color.block_success_back_color)
        icon.setImageResource(R.drawable.ic_verified)
        icon.setColorFilter(resources.getColor(R.color.block_success_color))
        icon.show()
        progress.hide()
        text.setTextColor(resources.getColor(R.color.block_success_text_color))
        actionButton.setRippleColorResource(R.color.block_success_color)
        actionButton.setTextColor(resources.getColor(R.color.block_success_text_color))
    }

    override fun setSecurityTypeSuspicious() {
        setBackgroundColor(R.color.block_warning_back_color)
        icon.setImageResource(R.drawable.ic_warning)
        icon.setColorFilter(resources.getColor(R.color.block_warning_color))
        icon.show()
        progress.hide()
        text.setTextColor(resources.getColor(R.color.block_warning_text_color))
        actionButton.setRippleColorResource(R.color.block_warning_color)
        actionButton.setTextColor(resources.getColor(R.color.block_warning_text_color))
    }

    override fun setSecurityTypeMalware() {
        setBackgroundColor(R.color.block_error_back_color)
        icon.setImageResource(R.drawable.ic_virus)
        icon.setColorFilter(resources.getColor(R.color.block_error_color))
        icon.show()
        progress.hide()
        text.setTextColor(resources.getColor(R.color.block_error_text_color))
        actionButton.setRippleColorResource(R.color.block_error_color)
        actionButton.setTextColor(resources.getColor(R.color.block_error_text_color))
    }

    override fun setSecurityTypeUnknown() {
        setBackgroundColor(R.color.block_info_back_color)
        icon.setImageResource(R.drawable.ic_security)
        icon.setColorFilter(resources.getColor(R.color.block_info_color))
        icon.show()
        progress.hide()
        text.setTextColor(resources.getColor(R.color.block_info_text_color))
        actionButton.setRippleColorResource(R.color.block_info_color)
        actionButton.setTextColor(resources.getColor(R.color.block_info_text_color))
    }

    override fun hideActionButton() {
        actionButton.hide()
    }

    override fun showActionButton(label: String) {
        actionButton.text = label
        actionButton.show()
    }

    override fun setSecurityText(text: String) {
        this.text.bind(text)
    }

    private fun setBackgroundColor(colorRes: Int) {
        val backgroundTintList = ColorStateList.valueOf(resources.getColor(colorRes))
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

