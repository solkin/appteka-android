package com.tomclaw.appsend.screen.settings

import android.content.res.Resources
import com.tomclaw.appsend.R

interface SettingsResourceProvider {

    fun getPrefShowSystemKey(): String

    fun getPrefDarkThemeKey(): String

    fun getPrefThemeModeKey(): String

    fun getPrefDynamicColorsKey(): String

    fun getPrefSortOrderKey(): String

    fun getSystemAppsWarningTitle(): String

    fun getSystemAppsWarningMessage(): String

    fun getGotItButtonText(): String

    fun getSortOrderEntries(): Array<CharSequence>

    fun getSortOrderValues(): Array<CharSequence>

    fun getSortOrderDefault(): String

}

class SettingsResourceProviderImpl(
    private val resources: Resources
) : SettingsResourceProvider {

    override fun getPrefShowSystemKey(): String =
        resources.getString(R.string.pref_show_system)

    override fun getPrefDarkThemeKey(): String =
        resources.getString(R.string.pref_dark_theme)

    override fun getPrefThemeModeKey(): String =
        resources.getString(R.string.pref_theme_mode)

    override fun getPrefDynamicColorsKey(): String =
        resources.getString(R.string.pref_dynamic_colors)

    override fun getPrefSortOrderKey(): String =
        resources.getString(R.string.pref_sort_order)

    override fun getSystemAppsWarningTitle(): String =
        resources.getString(R.string.system_apps_warning_title)

    override fun getSystemAppsWarningMessage(): String =
        resources.getString(R.string.system_apps_warning_message)

    override fun getGotItButtonText(): String =
        resources.getString(R.string.got_it)

    override fun getSortOrderEntries(): Array<CharSequence> =
        resources.getTextArray(R.array.pref_sort_order_strings)

    override fun getSortOrderValues(): Array<CharSequence> =
        resources.getTextArray(R.array.pref_sort_order_values)

    override fun getSortOrderDefault(): String =
        resources.getString(R.string.pref_sort_order_default)

}
