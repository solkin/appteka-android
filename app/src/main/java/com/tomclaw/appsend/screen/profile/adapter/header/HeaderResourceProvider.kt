package com.tomclaw.appsend.screen.profile.adapter.header

import android.content.Context
import android.text.format.DateUtils
import com.tomclaw.appsend.R
import java.util.concurrent.TimeUnit

interface HeaderResourceProvider {

    fun getRoleName(role: Int): String

    fun formatLastSeen(lastSeen: Long, onlineGap: Long): String

    fun formatJoinedTime(joined: Long): String

}

class HeaderResourceProviderImpl(
    private val context: Context,
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
            else -> context.getString(R.string.last_seen, timeDiff(lastSeen))
        }
        return lastSeenString.replace(' ', '\u00A0')
    }

    override fun formatJoinedTime(joined: Long): String {
        return context.getString(
            R.string.joined_date,
            timeDiff(joined)
        ).replace(' ', '\u00A0')
    }

    private fun timeDiff(time: Long): String {
        val current = System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(current - time).toInt()
        val months = days * 12 / 365
        val years = days / 365

        val isToday = DateUtils.isToday(time)
        val isYesterday = days == 1
        val isMonth = months == 0
        val isYear = years == 0

        return when {
            isToday -> context.getString(R.string.today)
            isYesterday -> context.getString(R.string.yesterday)
            isMonth -> context.resources.getQuantityString(R.plurals.days_ago, days, days)
            isYear -> context.resources.getQuantityString(R.plurals.months_ago, months, months)
            else -> context.resources.getQuantityString(R.plurals.years_ago, years, years)
        }
    }

}

private const val ROLE_OWNER = 300
private const val ROLE_ADMIN = 200
private const val ROLE_MODERATOR = 100
private const val ROLE_DEFAULT = 0
