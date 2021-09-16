package com.tomclaw.appsend.screen.moderation.adapter.app

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface AppItemView : ItemView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setSubtitle(subtitle: String?)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class AppItemViewHolder(view: View) : BaseViewHolder(view), AppItemView {

    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val title: TextView = view.findViewById(R.id.app_name)
    private val subtitle: TextView = view.findViewById(R.id.app_version)

    private var listener: (() -> Unit)? = null

    init {
        view.setOnClickListener { listener?.invoke() }
    }

    override fun setIcon(url: String?) {
//        GlideApp.with(icon)
//            .load(url)
//            .placeholder(R.drawable.ic_avatar_placeholder)
//            .circleCrop()
//            .into(icon)
    }

    override fun setTitle(title: String) {
//        this.title.bind(title)
    }

    override fun setSubtitle(subtitle: String?) {
//        this.subtitle.bind(subtitle)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.listener = listener
    }

    override fun onUnbind() {
        this.listener = null
    }

}
