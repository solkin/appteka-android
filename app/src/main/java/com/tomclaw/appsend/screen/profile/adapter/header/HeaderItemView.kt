package com.tomclaw.appsend.screen.profile.adapter.header

import android.view.View
import android.widget.TextView
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

}

class HeaderItemViewHolder(view: View) : BaseViewHolder(view), HeaderItemView {

    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.user_icon))
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val userDescription: TextView = view.findViewById(R.id.user_description)
    private val userOnline: View = view.findViewById(R.id.user_online)

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

    override fun onUnbind() {
    }

}
