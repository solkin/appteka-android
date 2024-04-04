package com.tomclaw.appsend.screen.profile.adapter.favorites

import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface FavoritesItemView : ItemView {

    fun setCount(count: Int)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class FavoritesItemViewHolder(view: View) : BaseViewHolder(view), FavoritesItemView {

    private val context = view.context
    private val subtitle: TextView = view.findViewById(R.id.subtitle)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setCount(count: Int) {
        this.subtitle.bind(context.getString(R.string.favorite_apps_count, count))
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
