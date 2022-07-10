package com.tomclaw.appsend.screen.details.adapter.header

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder

interface HeaderItemView : ItemView {

    fun setAppIcon(url: String?)

    fun setAppLabel(label: String?)

    fun setAppPackage(packageName: String?)

    fun showUploader()

    fun hideUploader()

    fun setUploaderIcon(userIcon: UserIcon)

    fun setUploaderName(name: String)

    fun setOnUploaderClickListener(listener: (() -> Unit)?)

}

class HeaderItemViewHolder(view: View) : BaseViewHolder(view), HeaderItemView {

    private val appIcon: ImageView = view.findViewById(R.id.app_icon)
    private val appLabel: TextView = view.findViewById(R.id.app_label)
    private val appPackage: TextView = view.findViewById(R.id.app_package)
    private val uploaderContainer: View = view.findViewById(R.id.uploader_container)
    private val uploaderIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.uploader_icon))
    private val uploaderName: TextView = view.findViewById(R.id.uploader_name)

    private var uploaderClickListener: (() -> Unit)? = null

    init {
        uploaderContainer.setOnClickListener { uploaderClickListener?.invoke() }
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

    override fun showUploader() {
        uploaderContainer.show()
    }

    override fun hideUploader() {
        uploaderContainer.hide()
    }

    override fun setUploaderIcon(userIcon: UserIcon) {
        this.uploaderIcon.bind(userIcon)
    }

    override fun setUploaderName(name: String) {
        this.uploaderName.bind(name)
    }

    override fun setOnUploaderClickListener(listener: (() -> Unit)?) {
        this.uploaderClickListener = listener
    }

    override fun onUnbind() {
        this.uploaderClickListener = null
    }

}
