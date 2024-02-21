package com.tomclaw.appsend.screen.profile.adapter.header

import android.content.Context
import android.text.format.DateUtils
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.TimeHelper
import java.text.DateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

interface HeaderResourceProvider {

    fun getRoleName(role: Int): String

    fun formatLastSeen(lastSeen: Long, onlineGapMinutes: Int): String

    fun formatJoinedTime(joined: Long): String

}

class HeaderResourceProviderImpl(
    private val context: Context,
    private val timeFormatter: DateFormat,
    private val dateFormatter: DateFormat,
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

    override fun formatLastSeen(lastSeen: Long, onlineGapMinutes: Int): String {
        val lastSeenTime = Calendar.getInstance()
        lastSeenTime.setTimeInMillis(lastSeen)
        val now = Calendar.getInstance()
        val currentTime = System.currentTimeMillis()
        val lastSeenMinutesDiff = TimeUnit.MILLISECONDS.toMinutes(currentTime - lastSeen)
        val isOnline = lastSeenMinutesDiff < onlineGapMinutes
        val isToday = DateUtils.isToday(lastSeen)
        val isYesterday = now[Calendar.DATE] - lastSeenTime[Calendar.DATE] == 1
        val lastSeenString = when {
            isOnline -> context.getString(R.string.online)
            isToday -> context.getString(R.string.today, timeFormatter.format(lastSeen))
            isYesterday -> context.getString(R.string.yesterday)
            lastSeen != 0L -> context.getString(R.string.last_seen, dateFormatter.format(lastSeen))
            else -> context.getString(R.string.offline)
        }
        return lastSeenString
    }

    override fun formatJoinedTime(joined: Long): String {
        return context.getString(
            R.string.joined_date,
            dateFormatter.format(joined)
        )
    }

}

private const val ROLE_OWNER = 300
private const val ROLE_ADMIN = 200
private const val ROLE_MODERATOR = 100
private const val ROLE_DEFAULT = 0
