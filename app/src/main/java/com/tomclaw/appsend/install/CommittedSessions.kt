package com.tomclaw.appsend.install

import android.content.Context
import androidx.core.content.edit

/**
 * Sessions handed to the system, by install key. The system answers them whenever the
 * user does, which can be after our process is gone, and only these may be waiting.
 */
internal class CommittedSessions(context: Context) {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun put(key: String, id: Int) = preferences.edit { putInt(key, id) }

    fun remove(key: String) = preferences.edit { remove(key) }

    fun all(): Map<String, Int> = preferences.all
        .mapNotNull { (key, id) -> (id as? Int)?.let { key to it } }
        .toMap()

}

private const val PREFERENCES_NAME = "committed_install_sessions"
