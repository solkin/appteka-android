package com.tomclaw.appsend.screen.feed.adapter.text

import android.net.Uri
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
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl
import com.tomclaw.imageloader.util.fetch

interface PostItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserName(name: String)

    fun setImage(uri: Uri)

    fun setText(text: String)

    fun setTime(time: String)

    fun showProgress()

    fun hideProgress()

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setClickable(clickable: Boolean)

}

class PostItemViewHolder(view: View) : BaseViewHolder(view), PostItemView {

    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val time: TextView = view.findViewById(R.id.date_view)
    private val image: ImageView = view.findViewById(R.id.image)
    private val text: TextView = view.findViewById(R.id.text)
    private val progress: View = view.findViewById(R.id.item_progress)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setUserIcon(userIcon: UserIcon) {
        this.userIcon.bind(userIcon)
    }

    override fun setUserName(name: String) {
        userName.bind(name)
    }

    override fun setImage(uri: Uri) {
        image.setImageResource(R.drawable.ic_cloud)
        image.scaleType = ImageView.ScaleType.CENTER_INSIDE
        image.fetch(uri) {
            placeholder = {
                with(it.get()) {
                    image.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                    image.setImageResource(com.tomclaw.appsend.R.drawable.ic_cloud)
                }
            }
            success = { viewHolder, result ->
                with(viewHolder.get()) {
                    image.scaleType = android.widget.ImageView.ScaleType.MATRIX
                    setImageDrawable(result.getDrawable())
                }
            }
            error = {
                with(it.get()) {
                    image.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                    image.setImageResource(com.tomclaw.appsend.R.drawable.ic_error)
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
        progress.visibility = VISIBLE
    }

    override fun hideProgress() {
        progress.visibility = GONE
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setClickable(clickable: Boolean) {
        itemView.isClickable = clickable
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
