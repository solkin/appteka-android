package com.tomclaw.appsend.install

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.tomclaw.appsend.R

interface InstallPreferences {

    fun method(): InstallMethodType

    fun setMethod(method: InstallMethodType)

}

class InstallPreferencesImpl(
    private val context: Context,
) : InstallPreferences {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    override fun method(): InstallMethodType = InstallMethodType.fromValue(
        preferences.getString(context.getString(R.string.pref_install_method), null)
    )

    override fun setMethod(method: InstallMethodType) {
        preferences.edit {
            putString(context.getString(R.string.pref_install_method), method.value)
        }
    }

}
