package com.tomclaw.appsend.screen.details.adapter.header

import android.content.res.ColorStateList
import android.graphics.Color.parseColor
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.scaleWithAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch
import com.tomclaw.imageloader.util.withPlaceholder


interface HeaderItemView : ItemView {

    fun setIndeterminate()

    fun setProgress(progress: Int)

    fun hideProgress()

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

    private val progressBar: CircularProgressIndicator = view.findViewById(R.id.progress_bar)
    private val appIcon: ImageView = view.findViewById(R.id.app_icon)
    private val iconBack: View = view.findViewById(R.id.icon_back)
    private val appLabel: TextView = view.findViewById(R.id.app_label)
    private val appPackage: TextView = view.findViewById(R.id.app_package)
    private val uploaderContainer: View = view.findViewById(R.id.uploader_container)
    private val uploaderIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.uploader_icon))
    private val uploaderName: TextView = view.findViewById(R.id.uploader_name)

    private var uploaderClickListener: (() -> Unit)? = null

    init {
        uploaderContainer.setOnClickListener { uploaderClickListener?.invoke() }
    }

    override fun setIndeterminate() {
        if (!progressBar.isVisible || iconBack.scaleX == 1.0f) {
            progressBar.show()
            iconBack.scaleWithAnimation(0.6f)
        }
        progressBar.isIndeterminate = true
    }

    override fun setProgress(progress: Int) {
        if (!progressBar.isVisible || iconBack.scaleX == 1.0f) {
            progressBar.show()
            iconBack.scaleWithAnimation(0.6f)
        }
        val animated = !progressBar.isIndeterminate
        progressBar.isIndeterminate = false
        progressBar.setProgressCompat(progress, animated)
    }

    override fun hideProgress() {
        if (progressBar.isVisible || iconBack.scaleX != 1.0f) {
            progressBar.hide()
            iconBack.scaleWithAnimation(1.0f)
        }
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
        val color = parseColor(userIcon.color)
        this.uploaderContainer.backgroundTintList = ColorStateList.valueOf(color)
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
