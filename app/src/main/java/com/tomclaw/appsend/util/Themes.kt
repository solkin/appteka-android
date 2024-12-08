package com.tomclaw.appsend.util

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import androidx.core.content.ContextCompat
import com.tomclaw.appsend.R

/**
 * Created by ivsolkin on 21.09.16.
 */

fun Activity.updateTheme(): Boolean {
    val isDarkTheme = isDarkTheme()
    setTheme(if (isDarkTheme) R.style.AppThemeBlack else R.style.AppTheme)
    return isDarkTheme
}

fun Activity.restartIfThemeChanged(isDarkTheme: Boolean) {
    if (isDarkTheme != isDarkTheme()) {
        val intent = intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        ContextCompat.startActivity(this, intent, null)
    }
}

fun Context.isDarkTheme(): Boolean {
    val preferences = getSharedPreferences(
        packageName + "_preferences", MODE_PRIVATE
    )
    return preferences.getBoolean(DARK_THEME_PREF_KEY, false)
}

const val DARK_THEME_PREF_KEY = "pref_dark_theme"
