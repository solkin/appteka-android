package com.tomclaw.appsend.screen.upload

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

interface UploadPreferencesProvider {

    fun isDarkTheme(): Boolean

    fun isAgreementAccepted(): Boolean

    fun setAgreementAccepted(value: Boolean)

}

class UploadPreferencesProviderImpl(
    context: Context
) : UploadPreferencesProvider {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

    override fun isDarkTheme(): Boolean {
        return preferences.getBoolean(DARK_THEME_PREF_KEY, false)
    }

    override fun isAgreementAccepted(): Boolean {
        return preferences.getBoolean(UPLOAD_AGREEMENT_KEY, false)
    }

    override fun setAgreementAccepted(value: Boolean) {
        preferences.edit().putBoolean(UPLOAD_AGREEMENT_KEY, value).apply()
    }

}

const val DARK_THEME_PREF_KEY = "pref_dark_theme"
const val UPLOAD_AGREEMENT_KEY = "pref_upload_agreement"
