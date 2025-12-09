package com.tomclaw.appsend.screen.details.adapter.status

import android.view.View
import android.widget.ImageView
import android.widget.TextView
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

    private val background: View = view.findViewById(R.id.status_back)
    private val icon: ImageView = view.findViewById(R.id.status_icon)
    private val text: TextView = view.findViewById(R.id.status_text)
    private val actionButton: MaterialButton = view.findViewById(R.id.action_button)

    private var actionClickListener: (() -> Unit)? = null

    init {
        actionButton.setOnClickListener { actionClickListener?.invoke() }
    }

    override fun setStatusTypeInfo() {
        // Only setting the icon resource. Icon color and text color 
        // must be controlled via XML using Theme Attributes (e.g., ?attr/colorPrimary)
        icon.setImageResource(R.drawable.ic_info)
    }

    override fun setStatusTypeWarning() {
        // Only setting the icon resource. Colors are controlled by XML/Theme Attributes.
        icon.setImageResource(R.drawable.ic_warning)
    }

    override fun setStatusTypeError() {
        // Only setting the icon resource. Colors are controlled by XML/Theme Attributes.
        icon.setImageResource(R.drawable.ic_error)
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

    override fun setOnActionClickListener(listener: (() -> Unit)?) {
        this.actionClickListener = listener
    }

    override fun onUnbind() {
        this.actionClickListener = null
    }
}