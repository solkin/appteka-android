package com.tomclaw.appsend.util

import android.app.Activity
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.tomclaw.appsend.R

class ThemeManager(private val app: Application) : Application.ActivityLifecycleCallbacks {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(app)
    private val activityFingerprints = HashMap<Activity, Long>()

    fun init() {
        migrateOldThemePreference()
        applyNightMode()
        app.registerActivityLifecycleCallbacks(this)
    }

    fun isDynamicColorsEnabled(): Boolean {
        val key = app.getString(R.string.pref_dynamic_colors)
        return prefs.getBoolean(key, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    }

    fun getSeedColor(): Int = prefs.getInt(KEY_SEED_COLOR, DEFAULT_SEED_COLOR)

    fun setSeedColor(color: Int) {
        prefs.edit().putInt(KEY_SEED_COLOR, color).apply()
    }

    fun applyNightMode() {
        val modeKey = app.getString(R.string.pref_theme_mode)
        val defaultMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        } else {
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
        var mode = prefs.getInt(modeKey, defaultMode)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
            && mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ) {
            mode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun migrateOldThemePreference() {
        val oldKey = app.getString(R.string.pref_dark_theme)
        val modeKey = app.getString(R.string.pref_theme_mode)
        if (prefs.contains(oldKey)) {
            val dark = prefs.getBoolean(oldKey, false)
            val mode =
                if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            prefs.edit().putInt(modeKey, mode).remove(oldKey).apply()
        }
    }

    private fun buildDynamicColorsOptions(): DynamicColorsOptions {
        return if (isDynamicColorsEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColorsOptions.Builder().build()
        } else {
            DynamicColorsOptions.Builder()
                .setContentBasedSource(getSeedColor())
                .build()
        }
    }

    private fun getColorsFingerprint(): Long {
        val dynamic =
            if (isDynamicColorsEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 1L
            else 0L
        val seed = getSeedColor().toLong() and 0xFFFFFFFFL
        return (dynamic shl 32) or seed
    }

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        DynamicColors.applyToActivityIfAvailable(activity, buildDynamicColorsOptions())
        activityFingerprints[activity] = getColorsFingerprint()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        val stored = activityFingerprints[activity]
        val current = getColorsFingerprint()
        if (stored != null && stored != current) {
            activityFingerprints[activity] = current
            activity.recreate()
        }
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        activityFingerprints.remove(activity)
    }

    companion object {
        private const val KEY_SEED_COLOR = "seed_color"
        const val DEFAULT_SEED_COLOR = 0xFF32A304.toInt()
    }
}
