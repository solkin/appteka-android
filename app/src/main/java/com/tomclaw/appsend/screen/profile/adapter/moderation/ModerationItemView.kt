package com.tomclaw.appsend.screen.profile.adapter.moderation

import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface ModerationItemView : ItemView {

    fun setCount(count: Int)

    fun setNoApps()

    fun showIndicator()

    fun hideIndicator()

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ModerationItemViewHolder(view: View) : BaseViewHolder(view), ModerationItemView {

    private val context = view.context
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val indicator: View = view.findViewById(R.id.indicator)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setCount(count: Int) {
        this.subtitle.bind(
            context.resources.getQuantityString(
                R.plurals.moderation_apps_count,
                count,
                count
            )
        )
    }

    override fun setNoApps() {
        this.subtitle.bind(context.getString(R.string.no_apps_on_moderation))
    }

    override fun showIndicator() {
        indicator.show()
    }

    override fun hideIndicator() {
        indicator.hide()
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}

