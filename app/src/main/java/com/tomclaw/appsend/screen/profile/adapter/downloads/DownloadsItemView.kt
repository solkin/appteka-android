package com.tomclaw.appsend.screen.profile.adapter.downloads

import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface DownloadsItemView : ItemView {

    fun setCount(count: Int)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class DownloadsItemViewHolder(view: View) : BaseViewHolder(view), DownloadsItemView {

    private val context = view.context
    private val subtitle: TextView = view.findViewById(R.id.subtitle)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setCount(count: Int) {
        this.subtitle.bind(
            context.resources.getQuantityString(
                R.plurals.download_apps_count,
                count,
                count
            )
        )
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
