package com.tomclaw.appsend.screen.installed

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.tomclaw.appsend.screen.installed.InstalledPreferencesProviderImpl.SortOrder

interface InstalledPreferencesProvider {

    fun isDarkTheme(): Boolean

    fun isShowSystemApps(): Boolean

    fun getSortOrder(): SortOrder

}

class InstalledPreferencesProviderImpl(
    context: Context
) : InstalledPreferencesProvider {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

    override fun isDarkTheme(): Boolean {
        return preferences.getBoolean(DARK_THEME_PREF_KEY, false)
    }

    override fun isShowSystemApps(): Boolean {
        return preferences.getBoolean(SHOW_SYSTEM_APPS_PREF_KEY, false)
    }

    override fun getSortOrder(): SortOrder {
        val order = preferences.getString(SORT_ORDER_PREF_KEY, SORT_ORDER_ASCENDING)
        return when (order) {
            SORT_ORDER_ASCENDING -> SortOrder.ASCENDING
            SORT_ORDER_DESCENDING -> SortOrder.DESCENDING
            SORT_ORDER_APP_SIZE -> SortOrder.APP_SIZE
            SORT_ORDER_INSTALL_TIME -> SortOrder.INSTALL_TIME
            SORT_ORDER_UPDATE_TIME -> SortOrder.UPDATE_TIME
            else -> SortOrder.ASCENDING
        }
    }

    enum class SortOrder {
        ASCENDING,
        DESCENDING,
        APP_SIZE,
        INSTALL_TIME,
        UPDATE_TIME,
    }

}
const val DARK_THEME_PREF_KEY = "pref_dark_theme"
const val SHOW_SYSTEM_APPS_PREF_KEY = "pref_show_system"
const val SORT_ORDER_PREF_KEY = "pref_sort_order"
const val SORT_ORDER_ASCENDING = "sort_order_ascending"
const val SORT_ORDER_DESCENDING = "sort_order_descending"
const val SORT_ORDER_APP_SIZE = "sort_order_app_size"
const val SORT_ORDER_INSTALL_TIME = "sort_order_install_time"
const val SORT_ORDER_UPDATE_TIME = "sort_order_update_time"
