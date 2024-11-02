package com.tomclaw.appsend.screen.users.adapter

import android.content.Context
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.TimeProvider

interface UsersResourceProvider {

    fun formatSubscribedDate(date: Long): String

}

class UsersResourceProviderImpl(
    private val context: Context,
    private val timeProvider: TimeProvider,
) : UsersResourceProvider {

    override fun formatSubscribedDate(date: Long): String {
        return context.getString(
            R.string.subscribed_date,
            timeProvider.formatTimeDiff(date)
        ).replace(' ', '\u00A0')
    }

}
