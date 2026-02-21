package com.tomclaw.appsend.util

import android.content.Context
import android.content.res.Configuration

fun Context.isDarkTheme(): Boolean {
    val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMode == Configuration.UI_MODE_NIGHT_YES
}
