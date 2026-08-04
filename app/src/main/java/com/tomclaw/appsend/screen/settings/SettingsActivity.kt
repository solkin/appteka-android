package com.tomclaw.appsend.screen.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.settings.di.SettingsActivityModule
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class SettingsActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent
            .settingsActivityComponent(SettingsActivityModule(context = this))
            .inject(activity = this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.settings_activity)

        setupToolbar()

        if (savedInstanceState == null) {
            analytics.trackEvent("open-settings-screen")
            supportFragmentManager.beginTransaction()
                .replace(R.id.content, SettingsFragment())
                .commit()
            // Opened at a sub-screen: the root goes on first anyway, so
            // back from there lands in settings rather than out of them.
            intent.getStringExtra(EXTRA_SCREEN_KEY)?.let { openScreen(it) }
        }
    }

    /**
     * A preference that is itself a screen opens the same fragment
     * rooted at that branch. Nothing here is settings-specific — it is
     * the navigation androidx.preference expects the host to provide.
     */
    override fun onPreferenceStartScreen(
        caller: PreferenceFragmentCompat,
        pref: PreferenceScreen
    ): Boolean {
        openScreen(pref.key)
        return true
    }

    private fun openScreen(key: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.content, SettingsFragment.forScreen(key))
            .addToBackStack(null)
            .commit()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            // Up out of a sub-screen goes back to the settings list, not
            // out of settings altogether.
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}

fun createSettingsActivityIntent(context: Context): Intent =
    Intent(context, SettingsActivity::class.java)

/**
 * Settings opened straight at the content filter — where a chip saying
 * something is hidden has to land. Anything else would leave the reader
 * hunting for the setting the chip just told them about.
 */
fun createContentFilterIntent(context: Context): Intent =
    createSettingsActivityIntent(context)
        .putExtra(EXTRA_SCREEN_KEY, context.getString(R.string.pref_content_filter_key))

private const val EXTRA_SCREEN_KEY = "screen_key"

