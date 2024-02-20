package com.tomclaw.appsend.screen.profile.adapter.header

import android.content.Context
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.RoleHelper

interface HeaderResourceProvider {

    fun getRoleName(role: Int): String

}

class HeaderResourceProviderImpl(private val context: Context) : HeaderResourceProvider {

    override fun getRoleName(role: Int): String {
        return context.getString(
            when (role) {
                RoleHelper.ROLE_OWNER -> R.string.role_owner
                RoleHelper.ROLE_ADMIN -> R.string.role_admin
                RoleHelper.ROLE_MODERATOR -> R.string.role_moderator
                else -> R.string.role_default
            }
        )
    }

}
