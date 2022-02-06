package com.tomclaw.appsend.screen.topics.adapter.topic

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
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

interface TopicItemView : ItemView {

    fun setIcon(url: String?)

    fun setTitle(title: String)

    fun setMessageText(text: String)

    fun setMessageAvatar(userIcon: UserIcon)

    fun showPin()

    fun hidePin()

    fun showProgress()

    fun hideProgress()

    fun showError()

    fun hideError()

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setOnLongClickListener(listener: (() -> Unit)?)

    fun setOnRetryListener(listener: (() -> Unit)?)

}

class TopicItemViewHolder(view: View) : BaseViewHolder(view), TopicItemView {

    private val icon: ImageView = view.findViewById(R.id.topic_icon)
    private val title: TextView = view.findViewById(R.id.topic_title)
    private val msgText: TextView = view.findViewById(R.id.msg_text)
    private val msgAvatar: UserIconView = UserIconViewImpl(view.findViewById(R.id.msg_avatar))
    private val topicPin: View = view.findViewById(R.id.topic_pin)
    private val progress: View = view.findViewById(R.id.item_progress)
    private val error: View = view.findViewById(R.id.error_view)
    private val retryButton: View = view.findViewById(R.id.button_retry)

    private var clickListener: (() -> Unit)? = null
    private var longClickListener: (() -> Unit)? = null
    private var retryListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
        view.setOnLongClickListener { longClickListener?.invoke()?.let { true } ?: false }
        retryButton.setOnClickListener { retryListener?.invoke() }
    }

    override fun setIcon(url: String?) {
        icon.fetch(url.orEmpty()) {
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

    override fun showProgress() {
        progress.visibility = VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = GONE
    }

    override fun showError() {
        error.visibility = VISIBLE
    }

    override fun hideError() {
        error.visibility = GONE
    }

    override fun setTitle(title: String) {
        this.title.bind(title)
    }

    override fun setMessageText(text: String) {
        this.msgText.bind(text)
    }

    override fun setMessageAvatar(userIcon: UserIcon) {
        this.msgAvatar.bind(userIcon)
    }

    override fun showPin() {
        topicPin.show()
    }

    override fun hidePin() {
        topicPin.hide()
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setOnLongClickListener(listener: (() -> Unit)?) {
        this.longClickListener = listener
    }

    override fun setOnRetryListener(listener: (() -> Unit)?) {
        this.retryListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
        this.retryListener = null
    }

}
