package com.tomclaw.appsend.screen.distro

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

interface DistroPreferencesProvider {

    fun isDarkTheme(): Boolean
    
    // New function to retrieve string resource by ID
    fun getString(resId: Int): String 

    // New function to retrieve string resource with format arguments
    fun getString(resId: Int, vararg formatArgs: Any): String 

}

class DistroPreferencesProviderImpl(
    private val context: Context // Context is needed to access resources
) : DistroPreferencesProvider {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

    override fun isDarkTheme(): Boolean {
        return preferences.getBoolean(DARK_THEME_PREF_KEY, false)
    }

    // Implementation to retrieve simple string resources
    override fun getString(resId: Int): String {
        return context.getString(resId)
    }

    // Implementation to retrieve formatted string resources
    override fun getString(resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}

const val DARK_THEME_PREF_KEY = "pref_dark_theme"
