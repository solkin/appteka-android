package com.tomclaw.appsend.screen.feed.adapter.favorite

import android.net.Uri
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

interface FavoriteItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserName(name: String)

    fun setIcon(url: String?)

    fun setLabel(value: String)

    fun setPackage(value: String)

    fun setImage(url: String?)

    fun hideImage()

    fun setText(text: String)

    fun setTime(time: String)

    fun showProgress()

    fun hideProgress()

    fun setOnPostClickListener(listener: (() -> Unit)?)

    fun setOnImageClickListener(listener: (() -> Unit)?)

    fun setClickable(clickable: Boolean)

}

class FavoriteItemViewHolder(view: View) : BaseViewHolder(view), FavoriteItemView {

    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val time: TextView = view.findViewById(R.id.date_view)
    private val icon: ImageView = view.findViewById(R.id.app_icon)
    private val label: TextView = view.findViewById(R.id.app_label)
    private val packageName: TextView = view.findViewById(R.id.app_package)
    private val text: TextView = view.findViewById(R.id.text)
    private val images: View = view.findViewById(R.id.images)
    private val card: View = view.findViewById(R.id.image_card_first)
    private val image: ImageView = view.findViewById(R.id.image_first)

    private var postClickListener: (() -> Unit)? = null
    private var imageClickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { postClickListener?.invoke() }
        card.setOnClickListener { imageClickListener?.invoke() }
    }

    override fun setUserIcon(userIcon: UserIcon) {
        this.userIcon.bind(userIcon)
    }

    override fun setUserName(name: String) {
        userName.bind(name)
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

    override fun setLabel(value: String) {
        label.bind(value)
    }

    override fun setPackage(value: String) {
        packageName.bind(value)
    }

    override fun setImage(url: String?) {
        images.show()
        image.fetch(url.orEmpty()) {
            centerCrop()
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
        }
    }

    override fun hideImage() {
        images.hide()
    }

    override fun setText(text: String) {
        this.text.bind(text)
    }

    override fun setTime(time: String) {
        this.time.bind(time)
    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    override fun setOnPostClickListener(listener: (() -> Unit)?) {
        this.postClickListener = listener
    }

    override fun setOnImageClickListener(listener: (() -> Unit)?) {
        this.imageClickListener = listener
    }

    override fun setClickable(clickable: Boolean) {
        itemView.isClickable = clickable
    }

    override fun onUnbind() {
        this.postClickListener = null
        this.imageClickListener = null
    }

}
