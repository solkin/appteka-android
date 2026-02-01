package com.tomclaw.appsend.screen.feed.adapter.text

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.feed.adapter.ReactionsAdapter
import com.tomclaw.appsend.screen.feed.adapter.ScreenshotsAdapter
import com.tomclaw.appsend.screen.feed.api.Reaction
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

    fun showMenu()

    fun hideMenu()

    fun setOnPostClickListener(listener: (() -> Unit)?)

    fun setOnImageClickListener(listener: (() -> Unit)?)

    fun setOnMenuClickListener(listener: (() -> Unit)?)

    fun setReactions(reactions: List<Reaction>)

    fun hideReactions()

    fun setOnReactionClickListener(listener: ((Reaction) -> Unit)?)

}

class TextItemViewHolder(
    view: View,
    private val adapter: ScreenshotsAdapter,
    private val reactionsAdapter: ReactionsAdapter,
) : BaseItemViewHolder(view), TextItemView {

    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val time: TextView = view.findViewById(R.id.date_view)
    private val text: TextView = view.findViewById(R.id.text)
    private val images: RecyclerView = view.findViewById(R.id.images)
    private val reactions: RecyclerView = view.findViewById(R.id.reactions)
    private val menu: View = view.findViewById(R.id.post_menu)

    private var postClickListener: (() -> Unit)? = null
    private var imageClickListener: (() -> Unit)? = null
    private var menuClickListener: (() -> Unit)? = null
    private var reactionClickListener: ((Reaction) -> Unit)? = null

    init {
        view.setOnClickListener { postClickListener?.invoke() }
        menu.setOnClickListener { menuClickListener?.invoke() }

        adapter.setHasStableIds(true)

        val orientation = RecyclerView.HORIZONTAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        images.adapter = adapter
        images.layoutManager = layoutManager
        images.itemAnimator = DefaultItemAnimator()
        images.itemAnimator?.changeDuration = DURATION_MEDIUM

        val reactionsOrientation = RecyclerView.HORIZONTAL
        val reactionsLayoutManager = LinearLayoutManager(view.context, reactionsOrientation, false)
        reactions.adapter = reactionsAdapter
        reactions.layoutManager = reactionsLayoutManager
        reactions.itemAnimator = DefaultItemAnimator()
        reactions.itemAnimator?.changeDuration = DURATION_MEDIUM
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

    override fun showMenu() {
        menu.show()
    }

    override fun hideMenu() {
        menu.hide()
    }

    override fun setOnPostClickListener(listener: (() -> Unit)?) {
        this.postClickListener = listener
    }

    override fun setOnImageClickListener(listener: (() -> Unit)?) {
        this.imageClickListener = listener
    }

    override fun setOnMenuClickListener(listener: (() -> Unit)?) {
        this.menuClickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun setReactions(reactions: List<Reaction>) {
        if (reactions.isNotEmpty()) {
            this.reactions.show()
            with(reactionsAdapter) {
                dataSet.clear()
                dataSet.addAll(reactions)
                setClickListener(reactionClickListener)
                notifyDataSetChanged()
            }
        } else {
            this.reactions.hide()
        }
    }

    override fun hideReactions() {
        reactions.hide()
    }

    override fun setOnReactionClickListener(listener: ((Reaction) -> Unit)?) {
        this.reactionClickListener = listener
        reactionsAdapter.setClickListener(listener)
    }

    override fun onUnbind() {
        this.postClickListener = null
        this.imageClickListener = null
        this.menuClickListener = null
    }

}

private const val DURATION_MEDIUM = 300L
