package com.tomclaw.appsend.util.bdui

import android.content.SharedPreferences
import com.google.gson.Gson

/**
 * Storage interface for BDUI store/load actions.
 * Abstracts away the persistence mechanism.
 */
interface BduiPreferencesStorage {

    fun store(key: String, value: Any?)

    fun load(key: String, defaultValue: Any?): Any?

}

/**
 * SharedPreferences-based implementation of BduiPreferencesStorage.
 */
class BduiPreferencesStorageImpl(
    private val preferences: SharedPreferences,
    private val gson: Gson
) : BduiPreferencesStorage {

    override fun store(key: String, value: Any?) {
        val prefKey = BDUI_PREF_PREFIX + key
        preferences.edit().apply {
            when (value) {
                null -> remove(prefKey)
                is Boolean -> putBoolean(prefKey, value)
                is Int -> putInt(prefKey, value)
                is Long -> putLong(prefKey, value)
                is Float -> putFloat(prefKey, value)
                is String -> putString(prefKey, value)
                else -> putString(prefKey, gson.toJson(value))
            }
        }.apply()
    }

    override fun load(key: String, defaultValue: Any?): Any? {
        val prefKey = BDUI_PREF_PREFIX + key
        return if (preferences.contains(prefKey)) {
            preferences.all[prefKey]
        } else {
            defaultValue
        }
    }

    companion object {
        private const val BDUI_PREF_PREFIX = "bdui_"
    }
}

