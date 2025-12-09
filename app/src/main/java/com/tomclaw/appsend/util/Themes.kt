package com.tomclaw.appsend.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.tomclaw.appsend.R

fun initTheme(app: Application) {
    val preferences = PreferenceManager.getDefaultSharedPreferences(app)
    val oldKey = app.getString(R.string.pref_dark_theme)
    val modeKey = app.getString(R.string.pref_theme_mode)

    // Migrate old boolean preference to new mode preference
    if (preferences.contains(oldKey)) {
        val dark = preferences.getBoolean(oldKey, false)
        val mode = if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        preferences.edit().putInt(modeKey, mode).remove(oldKey).apply()
    }

    applyTheme(app)
}

fun Activity.updateTheme() {
    applyTheme(this)
}

fun applyTheme(context: Context) {
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val modeKey = context.getString(R.string.pref_theme_mode)
    val dynamicKey = context.getString(R.string.pref_dynamic_colors)

    // Apply Dynamic Colors (Material You) on Android 12+
    val dynamicEnabled = preferences.getBoolean(dynamicKey, true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && dynamicEnabled) {
        DynamicColors.applyToActivitiesIfAvailable(context.applicationContext as Application)
    }

    // Determine default mode based on Android version
    val defaultMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    } else {
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
    }

    // Get and apply night mode
    var mode = preferences.getInt(modeKey, defaultMode)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
        mode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
    }
    AppCompatDelegate.setDefaultNightMode(mode)
}

/**
 * Returns true if the current theme is dark (night mode).
 * Uses the system configuration to determine the current UI mode.
 */
fun Context.isDarkTheme(): Boolean {
    val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return uiMode == Configuration.UI_MODE_NIGHT_YES
}
