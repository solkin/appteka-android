package com.tomclaw.appsend.core

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences

interface MigrationManager {

    fun isMigrationRequired(sinceBuildNumber: Long): Boolean

}

class MigrationManagerImpl(
    context: Context,
    appInfoProvider: AppInfoProvider,
) : MigrationManager {

    private val lastRunBuildNumber: Long

    private val preferences: SharedPreferences = context.getSharedPreferences(
        context.packageName + "_preferences", MODE_PRIVATE
    )

    init {
        lastRunBuildNumber = try {
            preferences.getLong(LAST_RUN_BUILD_NUMBER_PREF_KEY, 0)
        } catch (ex: ClassCastException) {
            preferences.getInt(LAST_RUN_BUILD_NUMBER_PREF_KEY, 0).toLong()
        }
        updateLastRunBuildNumber(appInfoProvider.getVersionCode())
    }

    override fun isMigrationRequired(sinceBuildNumber: Long): Boolean {
        return lastRunBuildNumber <= sinceBuildNumber
    }

    private fun updateLastRunBuildNumber(build: Long) {
        val editor = preferences.edit()
        with(editor) {
            putLong(LAST_RUN_BUILD_NUMBER_PREF_KEY, build)
            apply()
        }
    }

}

const val LAST_RUN_BUILD_NUMBER_PREF_KEY = "pref_last_run_build_number"
