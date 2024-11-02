package com.tomclaw.appsend.screen.profile.adapter.header

import android.content.Context
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.TimeProvider

interface HeaderResourceProvider {

    fun getRoleName(role: Int): String

    fun formatLastSeen(lastSeen: Long, onlineGap: Long): String

    fun formatJoinedTime(joined: Long): String

}

class HeaderResourceProviderImpl(
    private val context: Context,
    private val timeProvider: TimeProvider,
) : HeaderResourceProvider {

    override fun getRoleName(role: Int): String {
        return context.getString(
            when (role) {
                ROLE_OWNER -> R.string.role_owner
                ROLE_ADMIN -> R.string.role_admin
                ROLE_MODERATOR -> R.string.role_moderator
                else -> R.string.role_default
            }
        )
    }

    override fun formatLastSeen(lastSeen: Long, onlineGap: Long): String {
        val currentTime = System.currentTimeMillis()
        val isOffline = lastSeen == 0L
        val isOnline = currentTime - lastSeen < onlineGap
        val lastSeenString = when {
            isOffline -> context.getString(R.string.offline)
            isOnline -> context.getString(R.string.online)
            else -> context.getString(R.string.last_seen, timeProvider.formatTimeDiff(lastSeen))
        }
        return lastSeenString.replace(' ', '\u00A0')
    }

    override fun formatJoinedTime(joined: Long): String {
        return context.getString(
            R.string.joined_date,
            timeProvider.formatTimeDiff(joined)
        ).replace(' ', '\u00A0')
    }

}

private const val ROLE_OWNER = 300
private const val ROLE_ADMIN = 200
private const val ROLE_MODERATOR = 100
private const val ROLE_DEFAULT = 0
