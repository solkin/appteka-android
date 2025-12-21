package com.tomclaw.appsend.screen.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.applyTheme

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferences: SharedPreferences
    private lateinit var listener: OnSettingsChangedListener

    lateinit var toolbar: Toolbar
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        init()
    }

    fun init() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        listener = OnSettingsChangedListener()
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        preferences.registerOnSharedPreferenceChangeListener(listener)

        supportFragmentManager.beginTransaction()
            .replace(R.id.content, SettingsFragment())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::preferences.isInitialized && ::listener.isInitialized) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun actionHome(): Boolean {
        finish()
        return true
    }

    inner class OnSettingsChangedListener :
        SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(
            sp: SharedPreferences,
            key: String?
        ) {
            if (key == null) return

            when (key) {
                getString(R.string.pref_show_system) -> {
                    val show = sp.getBoolean(
                        getString(R.string.pref_show_system),
                        resources.getBoolean(R.bool.pref_show_system_default)
                    )
                    if (show) {
                        MaterialAlertDialogBuilder(this@SettingsActivity)
                            .setTitle(R.string.system_apps_warning_title)
                            .setMessage(R.string.system_apps_warning_message)
                            .setPositiveButton(R.string.got_it, null)
                            .show()
                    }
                    setResult(RESULT_OK)
                }

                getString(R.string.pref_theme_mode) -> {
                    recreate()
                    setResult(RESULT_OK)
                }

                getString(R.string.pref_dynamic_colors),
                getString(R.string.pref_sort_order) -> {
                    setResult(RESULT_OK)
                }
            }
        }
    }
}

fun createSettingsActivityIntent(context: Context): Intent {
    return Intent(context, SettingsActivity::class.java)
}