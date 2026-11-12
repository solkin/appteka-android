package com.tomclaw.appsend.screen.installed.adapter.app

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.scaleWithAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.imageloader.util.fetch

interface AppItemView : ItemView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setVersion(version: String)

    fun setSize(size: String)

    fun showBadge()

    fun hideBadge()

    fun setUpdateTime(time: String)

    fun setUpdateInfo(text: String?)

    fun showUpdateButton()

    fun showCancelButton()

    fun hideUpdateButton()

    fun showUpdateProgressIndeterminate()

    fun setUpdateProgress(percent: Int)

    fun hideUpdateProgress()

    fun setOtherEditionHint(value: Boolean)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setOnUpdateClickListener(listener: (() -> Unit)?)

}

class AppItemViewHolder(view: View) : BaseItemViewHolder(view), AppItemView {

    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val title: TextView = view.findViewById(R.id.app_name)
    private val version: TextView = view.findViewById(R.id.app_version)
    private val size: TextView = view.findViewById(R.id.app_size)
    private val badge: View = view.findViewById(R.id.badge_new)
    private val updateTime: TextView = view.findViewById(R.id.app_update_time)
    private val updateButton: MaterialButton = view.findViewById(R.id.update_button)
    private val updateInfo: TextView = view.findViewById(R.id.update_info)
    private val updateProgress: CircularProgressIndicator = view.findViewById(R.id.update_progress)
    private val iconBack: View = view.findViewById(R.id.icon_back)
    private val otherEditionHint: View = view.findViewById(R.id.other_edition_hint)

    private var iconUrl: String? = null
    private var clickListener: (() -> Unit)? = null
    private var updateClickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
        updateButton.setOnClickListener { updateClickListener?.invoke() }
    }

    override fun setIcon(url: String?) {
        // Progress rebinds the row many times; refetching would flash the placeholder.
        if (url == iconUrl) return
        iconUrl = url
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

    override fun showBadge() {
        badge.show()
    }

    override fun hideBadge() {
        badge.hide()
    }

    override fun setUpdateTime(time: String) {
        this.updateTime.bind(time)
    }

    override fun setUpdateInfo(text: String?) {
        updateInfo.bind(text)
    }

    override fun showUpdateButton() {
        updateButton.setText(R.string.update)
        updateButton.isVisible = true
    }

    override fun showCancelButton() {
        updateButton.setText(R.string.cancel)
        updateButton.isVisible = true
    }

    override fun hideUpdateButton() {
        updateButton.isVisible = false
    }

    override fun showUpdateProgressIndeterminate() {
        showUpdateProgress()
        updateProgress.isIndeterminate = true
    }

    override fun setUpdateProgress(percent: Int) {
        showUpdateProgress()
        val animated = !updateProgress.isIndeterminate
        updateProgress.isIndeterminate = false
        updateProgress.setProgressCompat(percent, animated)
    }

    override fun hideUpdateProgress() {
        if (updateProgress.isVisible || iconBack.scaleX != 1.0f) {
            updateProgress.hide()
            iconBack.scaleWithAnimation(1.0f)
        }
    }

    // The icon shrinks into the ring, so the progress takes no room of its own.
    private fun showUpdateProgress() {
        if (!updateProgress.isVisible || iconBack.scaleX == 1.0f) {
            updateProgress.show()
            iconBack.scaleWithAnimation(ICON_SCALE_IN_PROGRESS)
        }
    }

    override fun setOtherEditionHint(value: Boolean) {
        this.otherEditionHint.isVisible = value
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setOnUpdateClickListener(listener: (() -> Unit)?) {
        this.updateClickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
        this.updateClickListener = null
        iconUrl = null
        // A recycled row starts from rest instead of animating out of another app's progress.
        iconBack.animate().cancel()
        iconBack.scaleX = 1.0f
        iconBack.scaleY = 1.0f
        updateProgress.isVisible = false
    }

}

private const val ICON_SCALE_IN_PROGRESS = 0.7f
