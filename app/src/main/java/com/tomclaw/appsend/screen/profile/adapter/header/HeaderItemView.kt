package com.tomclaw.appsend.screen.profile.adapter.header

import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface HeaderItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserName(name: String)

    fun setUserDescription(value: String)

    fun setUserOnline(online: Boolean)

    fun showSubscribeButton()

    fun hideSubscribeButton()

    fun showUnsubscribeButton()

    fun hideUnsubscribeButton()

    fun showUserNameEditIcon()

    fun setOnNameClickListener(listener: (() -> Unit)?)

    fun setOnSubscribeClickListener(listener: (() -> Unit)?)

    fun setOnUnsubscribeClickListener(listener: (() -> Unit)?)

}

class HeaderItemViewHolder(private val view: View) : BaseViewHolder(view), HeaderItemView {

    private val resources = view.resources
    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.user_icon))
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val userDescription: TextView = view.findViewById(R.id.user_description)
    private val userOnline: View = view.findViewById(R.id.user_online)
    private val subscribeButton: View = view.findViewById(R.id.subscribe_button)
    private val unsubscribeButton: View = view.findViewById(R.id.unsubscribe_button)

    private var nameClickListener: (() -> Unit)? = null
    private var subscribeClickListener: (() -> Unit)? = null
    private var unsubscribeClickListener: (() -> Unit)? = null

    override fun setUserIcon(userIcon: UserIcon) {
        this.userIcon.bind(userIcon)
    }

    override fun setUserDescription(value: String) {
        userDescription.bind(value)
    }

    override fun setUserName(name: String) {
        userName.bind(name)
    }

    override fun setUserOnline(online: Boolean) {
        userOnline.isVisible = online
    }

    override fun showSubscribeButton() {
        subscribeButton.show()
    }

    override fun hideSubscribeButton() {
        subscribeButton.hide()
    }

    override fun showUnsubscribeButton() {
        unsubscribeButton.show()
    }

    override fun hideUnsubscribeButton() {
        unsubscribeButton.hide()
    }

    override fun showUserNameEditIcon() {
        userName.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ResourcesCompat.getDrawable(resources, R.drawable.ic_edit, null),
            null
        )
    }

    override fun setOnNameClickListener(listener: (() -> Unit)?) {
        this.nameClickListener = listener

        userName.setOnClickListener(listener?.let { { nameClickListener?.invoke() } })
    }

    override fun setOnSubscribeClickListener(listener: (() -> Unit)?) {
        subscribeClickListener = listener

        subscribeButton.setOnClickListener(listener?.let { { subscribeClickListener?.invoke() } })
    }

    override fun setOnUnsubscribeClickListener(listener: (() -> Unit)?) {
        unsubscribeClickListener = listener

        unsubscribeButton.setOnClickListener(listener?.let { { unsubscribeClickListener?.invoke() } })
    }

    override fun onUnbind() {
        this.nameClickListener = null
        this.subscribeClickListener = null
        this.unsubscribeClickListener = null
    }

}
