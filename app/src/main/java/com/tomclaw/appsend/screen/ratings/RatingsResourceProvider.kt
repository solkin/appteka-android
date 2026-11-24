package com.tomclaw.appsend.screen.ratings

import android.content.res.Resources
import com.tomclaw.appsend.util.formatReviewDate
import java.text.DateFormat

interface RatingsResourceProvider {

    fun formatReviewDate(time: Long, versionName: String?): String

}

class RatingsResourceProviderImpl(
    private val resources: Resources,
    private val dateFormatter: DateFormat,
) : RatingsResourceProvider {

    override fun formatReviewDate(time: Long, versionName: String?): String {
        return resources.formatReviewDate(dateFormatter, time, versionName)
    }

}
