package com.tomclaw.appsend.screen.upload.adapter.screen_append

import android.view.View
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R

interface ScreenAppendItemView : ItemView {

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ScreenAppendItemViewHolder(view: View) : BaseItemViewHolder(view), ScreenAppendItemView {

    private val appendScreenButton: View = view.findViewById(R.id.screen_append)

    private var clickListener: (() -> Unit)? = null

    init {
        appendScreenButton.setOnClickListener { clickListener?.invoke() }
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
