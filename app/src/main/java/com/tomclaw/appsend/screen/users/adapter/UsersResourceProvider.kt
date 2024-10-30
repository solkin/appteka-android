package com.tomclaw.appsend.screen.users.adapter

import android.content.Context
import android.text.format.DateUtils
import com.tomclaw.appsend.R
import java.util.concurrent.TimeUnit

interface UsersResourceProvider {

    fun formatSubscribedDate(date: Long): String

}

class UsersResourceProviderImpl(
    private val context: Context,
) : UsersResourceProvider {

    override fun formatSubscribedDate(date: Long): String {
        return context.getString(
            R.string.subscribed_date,
            timeDiff(date)
        ).replace(' ', '\u00A0')
    }

    private fun timeDiff(time: Long): String {
        val current = System.currentTimeMillis()
        val days = TimeUnit.MILLISECONDS.toDays(current - time).toInt()
        val months = days * 12 / 365
        val years = days / 365

        val isToday = DateUtils.isToday(time)
        val isYesterday = days <= 1 && !isToday
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
