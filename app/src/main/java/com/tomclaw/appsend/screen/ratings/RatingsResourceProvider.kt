package com.tomclaw.appsend.screen.ratings

import android.content.res.Resources
import com.tomclaw.appsend.R
import java.text.DateFormat

interface RatingsResourceProvider {

    fun formatReviewDate(time: Long, versionName: String?): String

}

class RatingsResourceProviderImpl(
    private val resources: Resources,
    private val dateFormatter: DateFormat,
) : RatingsResourceProvider {

    override fun formatReviewDate(time: Long, versionName: String?): String {
        val date = dateFormatter.format(time)
        val version = versionName?.trim()
        return if (version.isNullOrEmpty()) {
            date
        } else {
            resources.getString(R.string.review_date_with_version, date, version)
        }
    }

}
