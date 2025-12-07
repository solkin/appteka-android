package com.tomclaw.appsend.screen.settings

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import io.reactivex.rxjava3.core.Observable

interface SettingsView {

    fun showCacheClearedMessage()

    fun showCacheClearErrorMessage()

    fun showSystemAppsWarning(title: String, message: String, buttonText: String)

    fun clearCacheClicks(): Observable<Unit>

}

class SettingsViewImpl(
    private val fragment: Fragment
) : SettingsView {

    private val clearCacheRelay = PublishRelay.create<Unit>()

    override fun showCacheClearedMessage() {
        Toast.makeText(
            fragment.requireContext(),
            R.string.cache_cleared_successfully,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun showCacheClearErrorMessage() {
        Toast.makeText(
            fragment.requireContext(),
            R.string.cache_clearing_failed,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun showSystemAppsWarning(title: String, message: String, buttonText: String) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(buttonText, null)
            .create()
            .show()
    }

    override fun clearCacheClicks(): Observable<Unit> = clearCacheRelay

    fun onClearCacheClick() {
        clearCacheRelay.accept(Unit)
    }

}

