package com.tomclaw.appsend.screen.profile.adapter.header

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.getColor
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl


interface HeaderItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserRole(role: String)

    fun setUserName(name: String)

    fun setOnline(online: Boolean)

    fun setLastSeen(lastSeen: String)

    fun setJoined(joined: String)

}

class HeaderItemViewHolder(view: View) : BaseViewHolder(view), HeaderItemView {

    private val context = view.context
    private val userIcon: UserIconView = UserIconViewImpl(view.findViewById(R.id.user_icon))
    private val userRole: TextView = view.findViewById(R.id.user_role)
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val userOnline: TextView = view.findViewById(R.id.user_online)
    private val userJoined: TextView = view.findViewById(R.id.user_joined)

    override fun setUserIcon(userIcon: UserIcon) {
        this.userIcon.bind(userIcon)
    }

    override fun setUserRole(role: String) {
        userRole.bind(role)
    }

    override fun setUserName(name: String) {
        userName.bind(name)
    }

    override fun setOnline(online: Boolean) {
        if (online) {
            userOnline.setTextColor(getColor(R.color.online_color, context))
        } else {
            userOnline.setTextColor(R.attr.text_secondary_color)
        }
    }

    override fun setLastSeen(lastSeen: String) {
        userOnline.bind(lastSeen)
    }

    override fun setJoined(joined: String) {
        userJoined.bind(joined)
    }

    override fun onUnbind() {
    }

}
