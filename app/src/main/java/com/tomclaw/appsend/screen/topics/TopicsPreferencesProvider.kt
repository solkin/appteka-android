package com.tomclaw.appsend.screen.topics

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.tomclaw.appsend.screen.chat.DARK_THEME_PREF_KEY

interface TopicsPreferencesProvider {

    fun isDarkTheme(): Boolean

    fun isShowIntro(): Boolean

    fun setIntroShown()

}

class TopicsPreferencesProviderImpl(
    context: Context
) : TopicsPreferencesProvider {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

    override fun isDarkTheme(): Boolean {
        return preferences.getBoolean(DARK_THEME_PREF_KEY, false)
    }

    override fun isShowIntro(): Boolean {
        return preferences.getBoolean(DISCUSS_INTRO_PREF_KEY, true)
    }

    override fun setIntroShown() {
        val editor = preferences.edit()
        with(editor) {
            putBoolean(DISCUSS_INTRO_PREF_KEY, false)
            apply()
        }
    }

}

const val DARK_THEME_PREF_KEY = "pref_dark_theme"
const val DISCUSS_INTRO_PREF_KEY = "pref_discuss_intro"
