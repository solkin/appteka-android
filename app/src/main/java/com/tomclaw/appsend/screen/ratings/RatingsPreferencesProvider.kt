package com.tomclaw.appsend.screen.ratings

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

interface RatingsPreferencesProvider {

    fun isDarkTheme(): Boolean

}

class RatingsPreferencesProviderImpl(
    context: Context
) : RatingsPreferencesProvider {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

    override fun isDarkTheme(): Boolean {
        return preferences.getBoolean(DARK_THEME_PREF_KEY, false)
    }

}

const val DARK_THEME_PREF_KEY = "pref_dark_theme"
