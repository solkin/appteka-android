package com.tomclaw.appsend.screen.upload.adapter.other_versions

import android.view.View
import android.widget.TextView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R

interface OtherVersionsItemView : ItemView {

    fun setVersionsCount(value: Int)

    fun setOnClickListener(listener: (() -> Unit)?)

}

@Suppress("DEPRECATION")
class OtherVersionsItemViewHolder(view: View) : BaseItemViewHolder(view), OtherVersionsItemView {

    private val resources = view.resources
    private val otherVersionsText: TextView = view.findViewById(R.id.other_versions_text)
    private val otherVersionsButton: View = view.findViewById(R.id.other_versions_button)

    private var clickListener: (() -> Unit)? = null

    init {
        otherVersionsButton.setOnClickListener { clickListener?.invoke() }
    }

    override fun setVersionsCount(value: Int) {
        otherVersionsText.text =
            resources.getQuantityString(R.plurals.other_versions_text, value, value)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
