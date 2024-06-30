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
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface HeaderItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserName(name: String)

    fun setUserDescription(value: String)

    fun setUserOnline(online: Boolean)

    fun showUserNameEditIcon()

    fun setOnNameClickListener(listener: (() -> Unit)?)

}

class HeaderItemViewHolder(private val view: View) : BaseViewHolder(view), HeaderItemView {

    private val resources = view.resources
    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.user_icon))
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val userDescription: TextView = view.findViewById(R.id.user_description)
    private val userOnline: View = view.findViewById(R.id.user_online)

    private var nameClickListener: (() -> Unit)? = null

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

    override fun onUnbind() {
        this.nameClickListener = null
    }

}
