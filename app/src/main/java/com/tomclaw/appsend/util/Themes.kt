package com.tomclaw.appsend.util

import android.app.Activity
import android.content.Intent
import androidx.core.content.ContextCompat
import com.tomclaw.appsend.R

/**
 * Created by ivsolkin on 21.09.16.
 */

fun Activity.updateTheme(): Boolean {
    val isDarkTheme = PreferenceHelper.isDarkTheme(this)
    setTheme(if (isDarkTheme) R.style.AppThemeBlack else R.style.AppTheme)
    return isDarkTheme
}

fun Activity.restartIfThemeChanged(isDarkTheme: Boolean) {
    if (isDarkTheme != PreferenceHelper.isDarkTheme(this)) {
        val intent = intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        ContextCompat.startActivity(this, intent, null)
    }
}
