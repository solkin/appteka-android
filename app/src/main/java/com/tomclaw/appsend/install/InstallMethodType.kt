package com.tomclaw.appsend.install

import androidx.annotation.StringRes
import com.tomclaw.appsend.R

enum class InstallMethodType(
    val value: String,
    @param:StringRes val title: Int,
    @param:StringRes val summary: Int,
) {

    SESSION("session", R.string.app_name, R.string.pref_summary_install_method_session),
    SYSTEM("system", R.string.install_method_system, R.string.pref_summary_install_method_system);

    companion object {

        val DEFAULT = SESSION

        fun fromValue(value: String?): InstallMethodType =
            entries.firstOrNull { it.value == value } ?: DEFAULT

    }

}
