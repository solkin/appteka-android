package com.tomclaw.appsend.screen.upload.adapter.selected_app

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface SelectedAppItemView : ItemView {

    fun setOnClickListener(listener: (() -> Unit)?)

}

class SelectedAppItemViewHolder(view: View) : BaseViewHolder(view), SelectedAppItemView {

    private val appIcon: ImageView = view.findViewById(R.id.app_icon)
    private val appLabel: TextView = view.findViewById(R.id.app_label)
    private val appPackage: TextView = view.findViewById(R.id.app_package)
    private val appVersion: TextView = view.findViewById(R.id.app_version)
    private val appSize: TextView = view.findViewById(R.id.app_size)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
