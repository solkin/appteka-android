package com.tomclaw.appsend.util

import android.content.res.Resources
import com.tomclaw.appsend.R
import java.text.DateFormat

/**
 * Review date, followed by the version it was left on when there is one.
 * The format string adds its own "v", so a version name that already starts
 * with one ("v2.0") loses it instead of turning into "vv2.0".
 */
fun Resources.formatReviewDate(dateFormatter: DateFormat, time: Long, versionName: String?): String {
    val date = dateFormatter.format(time)
    val version = versionName?.trim()?.let { name ->
        val hasPrefix = name.length > 1 && name[0].equals('v', ignoreCase = true) && name[1].isDigit()
        if (hasPrefix) name.substring(1) else name
    }
    return if (version.isNullOrEmpty()) {
        date
    } else {
        getString(R.string.review_date_with_version, date, version)
    }
}
