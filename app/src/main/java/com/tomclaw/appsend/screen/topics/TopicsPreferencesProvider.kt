package com.tomclaw.appsend.screen.topics

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

interface TopicsPreferencesProvider {

    fun isShowIntro(): Boolean

    fun setIntroShown()

}

class TopicsPreferencesProviderImpl(
    context: Context
) : TopicsPreferencesProvider {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

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

const val DISCUSS_INTRO_PREF_KEY = "pref_discuss_intro"