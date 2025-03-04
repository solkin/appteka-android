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
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch

interface FavoriteItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserName(name: String)

    fun setImage(uri: Uri)

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
    private val text: TextView = view.findViewById(R.id.text)
    private val card: View = view.findViewById(R.id.image_card)
    private val image: ImageView = view.findViewById(R.id.image)

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

    override fun setImage(uri: Uri) {
        image.fetch(uri.toString()) {
            centerCrop()
            placeholder = {
                with(it.get()) {
                    setImageDrawable(null)
                }
            }
        }
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
