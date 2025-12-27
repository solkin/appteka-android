package com.tomclaw.appsend.screen.upload.adapter.selected_app

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder

interface SelectedAppItemView : ItemView {

    fun setAppIcon(url: String?)

    fun setAppLabel(label: String?)

    fun setAppPackage(packageName: String?)

    fun setAppVersion(version: String?)

    fun setAppSize(size: String?)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setOnDiscardListener(listener: (() -> Unit)?)

}

class SelectedAppItemViewHolder(private val cardView: View) : BaseViewHolder(cardView), SelectedAppItemView {

    private val appIcon: ImageView = cardView.findViewById(R.id.app_icon)
    private val appLabel: TextView = cardView.findViewById(R.id.app_label)
    private val appPackage: TextView = cardView.findViewById(R.id.app_package)
    private val appVersion: TextView = cardView.findViewById(R.id.app_version)
    private val appSize: TextView = cardView.findViewById(R.id.app_size)
    private val discardButton: View = cardView.findViewById(R.id.discard_button)

    private var clickListener: (() -> Unit)? = null
    private var discardListener: (() -> Unit)? = null

    init {
        cardView.setOnClickListener { clickListener?.invoke() }
        discardButton.setOnClickListener { discardListener?.invoke() }
    }

    override fun setAppIcon(url: String?) {
        appIcon.fetch(url.orEmpty()) {
            centerCrop()
            withPlaceholder(R.drawable.app_placeholder)
            placeholder = {
                with(it.get()) {
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(R.drawable.app_placeholder)
                }
            }
        }
    }

    override fun setAppLabel(label: String?) {
        this.appLabel.bind(label)
    }

    override fun setAppPackage(packageName: String?) {
        this.appPackage.bind(packageName)
    }

    override fun setAppVersion(version: String?) {
        this.appVersion.bind(version)
    }

    override fun setAppSize(size: String?) {
        this.appSize.bind(size)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setOnDiscardListener(listener: (() -> Unit)?) {
        this.discardListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
        this.discardListener = null
    }

}
