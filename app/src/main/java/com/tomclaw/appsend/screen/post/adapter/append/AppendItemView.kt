package com.tomclaw.appsend.screen.post.adapter.append

import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface AppendItemView : ItemView {

    fun setOnClickListener(listener: (() -> Unit)?)

}

class AppendItemViewHolder(view: View) : BaseViewHolder(view), AppendItemView {

    private val appendScreenButton: View = view.findViewById(R.id.image_append)

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
