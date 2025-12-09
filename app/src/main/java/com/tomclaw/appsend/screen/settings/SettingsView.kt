package com.tomclaw.appsend.screen.settings

import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
        fragment.view?.let { view ->
            Snackbar.make(view, R.string.cache_cleared_successfully, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showCacheClearErrorMessage() {
        fragment.view?.let { view ->
            Snackbar.make(view, R.string.cache_clearing_failed, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun showSystemAppsWarning(title: String, message: String, buttonText: String) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNeutralButton(buttonText, null)
            .create()
            .show()
    }

    override fun clearCacheClicks(): Observable<Unit> = clearCacheRelay

    fun onClearCacheClick() {
        showClearCacheConfirmation()
    }

    private fun showClearCacheConfirmation() {
        val context = fragment.requireContext()
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.clear_cache_dialog_title)
            .setMessage(context.getString(R.string.clear_cache_dialog_message, "Appteka"))
            .setPositiveButton(R.string.clear_cache_dialog_positive) { _, _ ->
                clearCacheRelay.accept(Unit)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

}
