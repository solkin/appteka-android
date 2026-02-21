package com.tomclaw.appsend.screen.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.settings.di.SettingsActivityModule
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class SettingsActivity : AppCompatActivity() {

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
        }
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
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}

fun createSettingsActivityIntent(context: Context): Intent =
    Intent(context, SettingsActivity::class.java)

