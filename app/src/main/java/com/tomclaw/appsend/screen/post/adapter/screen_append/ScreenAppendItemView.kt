package com.tomclaw.appsend.screen.post.adapter.screen_append

import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface ScreenAppendItemView : ItemView {

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ScreenAppendItemViewHolder(view: View) : BaseViewHolder(view), ScreenAppendItemView {

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
