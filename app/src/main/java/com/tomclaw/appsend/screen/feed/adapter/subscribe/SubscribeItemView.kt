package com.tomclaw.appsend.screen.feed.adapter.subscribe

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl
import androidx.core.graphics.toColorInt
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.screen.feed.adapter.ReactionsAdapter
import com.tomclaw.appsend.screen.feed.api.Reaction

interface SubscribeItemView : ItemView {

    fun setUserIcon(icon: UserIcon)

    fun setUserName(name: String)

    fun setPublisherIcon(icon: UserIcon)

    fun setPublisherName(name: String)

    fun setTime(time: String)

    fun showProgress()

    fun hideProgress()

    fun showMenu()

    fun hideMenu()

    fun setOnPostClickListener(listener: (() -> Unit)?)

    fun setOnPublisherClickListener(listener: (() -> Unit)?)

    fun setOnMenuClickListener(listener: (() -> Unit)?)

    fun setReactions(reactions: List<com.tomclaw.appsend.screen.feed.api.Reaction>)

    fun hideReactions()

}

class SubscribeItemViewHolder(
    view: View,
    private val reactionsAdapter: ReactionsAdapter,
) : BaseViewHolder(view), SubscribeItemView {

    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val publisherIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.uploader_icon))
    private val publisherName: TextView = view.findViewById(R.id.uploader_name)
    private val time: TextView = view.findViewById(R.id.date_view)
    private val publisherContainer: View = view.findViewById(R.id.uploader_container)
    private val reactions: RecyclerView = view.findViewById(R.id.reactions)
    private val menu: View = view.findViewById(R.id.post_menu)

    private var postClickListener: (() -> Unit)? = null
    private var subscribedClickListener: (() -> Unit)? = null
    private var menuClickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { postClickListener?.invoke() }
        menu.setOnClickListener { menuClickListener?.invoke() }
        publisherContainer.setOnClickListener { subscribedClickListener?.invoke() }

        val reactionsOrientation = RecyclerView.HORIZONTAL
        val reactionsLayoutManager = LinearLayoutManager(view.context, reactionsOrientation, false)
        reactions.adapter = reactionsAdapter
        reactions.layoutManager = reactionsLayoutManager
        reactions.itemAnimator = DefaultItemAnimator()
        reactions.itemAnimator?.changeDuration = 300L
    }

    override fun setUserIcon(icon: UserIcon) {
        userIcon.bind(icon)
    }

    override fun setUserName(name: String) {
        userName.bind(name)
    }

    override fun setPublisherIcon(icon: UserIcon) {
        publisherIcon.bind(icon)
        val color = icon.color.toColorInt()
        publisherContainer.backgroundTintList = ColorStateList.valueOf(color)
    }

    override fun setPublisherName(name: String) {
        publisherName.bind(name)
    }

    override fun setTime(time: String) {
        this.time.bind(time)
    }

    override fun showProgress() {
    }

    override fun hideProgress() {
    }

    override fun showMenu() {
        menu.show()
    }

    override fun hideMenu() {
        menu.hide()
    }

    override fun setOnPostClickListener(listener: (() -> Unit)?) {
        this.postClickListener = listener
    }

    override fun setOnPublisherClickListener(listener: (() -> Unit)?) {
        this.subscribedClickListener = listener
    }

    override fun setOnMenuClickListener(listener: (() -> Unit)?) {
        this.menuClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setReactions(reactionsList: List<Reaction>) {
        if (reactionsList.isNotEmpty()) {
            reactions.show()
            with(reactionsAdapter) {
                dataSet.clear()
                dataSet.addAll(reactionsList)
                notifyDataSetChanged()
            }
        } else {
            reactions.hide()
        }
    }

    override fun hideReactions() {
        reactions.hide()
    }

    override fun onUnbind() {
        this.postClickListener = null
        this.subscribedClickListener = null
    }

}
