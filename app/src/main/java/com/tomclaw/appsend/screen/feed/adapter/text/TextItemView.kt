package com.tomclaw.appsend.screen.feed.adapter.text

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.feed.adapter.ScreenshotsAdapter
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface TextItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserName(name: String)

    fun setImages(screenshots: List<Screenshot>)

    fun hideImage()

    fun setText(text: String)

    fun setTime(time: String)

    fun showProgress()

    fun hideProgress()

    fun setOnPostClickListener(listener: (() -> Unit)?)

    fun setOnImageClickListener(listener: (() -> Unit)?)

    fun setClickable(clickable: Boolean)

}

class TextItemViewHolder(
    view: View,
    private val adapter: ScreenshotsAdapter,
) : BaseViewHolder(view), TextItemView {

    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val time: TextView = view.findViewById(R.id.date_view)
    private val text: TextView = view.findViewById(R.id.text)
    private val images: RecyclerView = view.findViewById(R.id.images)

    private var postClickListener: (() -> Unit)? = null
    private var imageClickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { postClickListener?.invoke() }

        adapter.setHasStableIds(true)

        val orientation = RecyclerView.HORIZONTAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        images.adapter = adapter
        images.layoutManager = layoutManager
        images.itemAnimator = DefaultItemAnimator()
        images.itemAnimator?.changeDuration = DURATION_MEDIUM
    }

    override fun setUserIcon(userIcon: UserIcon) {
        this.userIcon.bind(userIcon)
    }

    override fun setUserName(name: String) {
        userName.bind(name)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setImages(screenshots: List<Screenshot>) {
        images.show()
        with(adapter) {
            dataSet.clear()
            dataSet.addAll(screenshots)
            notifyDataSetChanged()
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

private const val DURATION_MEDIUM = 300L
