package com.tomclaw.appsend.screen.details

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

interface DetailsPreferencesProvider {

    fun isDarkTheme(): Boolean

}

class DetailsPreferencesProviderImpl(
    context: Context
) : DetailsPreferencesProvider {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

    override fun isDarkTheme(): Boolean {
        return preferences.getBoolean(DARK_THEME_PREF_KEY, false)
    }

}

const val DARK_THEME_PREF_KEY = "pref_dark_theme"
