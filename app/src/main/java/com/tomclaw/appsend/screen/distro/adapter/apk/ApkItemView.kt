package com.tomclaw.appsend.screen.distro.adapter.apk

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.imageloader.util.fetch

interface ApkItemView : ItemView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setVersion(version: String)

    fun setSize(size: String)

    fun setLocation(path: String?)

    fun showBadge()

    fun hideBadge()

    fun setLastModified(time: String)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ApkItemViewHolder(view: View) : BaseViewHolder(view), ApkItemView {

    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val title: TextView = view.findViewById(R.id.app_name)
    private val version: TextView = view.findViewById(R.id.app_version)
    private val size: TextView = view.findViewById(R.id.app_size)
    private val badge: View = view.findViewById(R.id.badge_new)
    private val lastModified: TextView = view.findViewById(R.id.app_last_modified)
    private val location: TextView = view.findViewById(R.id.apk_location)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
            centerCrop()
            placeholder(R.drawable.app_placeholder)
            onLoading { imageView ->
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.setImageResource(R.drawable.app_placeholder)
            }
        }
    }

    override fun setTitle(title: String) {
        this.title.bind(title)
    }

    override fun setVersion(version: String) {
        this.version.bind(version)
    }

    override fun setSize(size: String) {
        this.size.bind(size)
    }

    override fun setLocation(path: String?) {
        location.bind(path)
    }

    override fun showBadge() {
        badge.show()
    }

    override fun hideBadge() {
        badge.hide()
    }

    override fun setLastModified(time: String) {
        this.lastModified.bind(time)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
