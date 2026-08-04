package com.tomclaw.appsend.screen.settings

import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.content.ContentFlag
import io.reactivex.rxjava3.core.Observable

interface SettingsView {

    fun showCacheClearedMessage()

    fun showCacheClearErrorMessage()

    fun showSystemAppsWarning(title: String, message: String, buttonText: String)

    fun clearCacheClicks(): Observable<Unit>

    /** Paints the switches for the content the account currently hides. */
    fun showContentFilter(flags: Set<ContentFlag>)

    /**
     * Greys the block out while the value is in flight or when there is
     * no account to hold it, with a summary saying which of the two.
     */
    fun setContentFilterEnabled(enabled: Boolean)

    fun showContentFilterUnavailable()

    fun showContentFilterError()

    /**
     * A switch was moved. Emits the selection as a whole rather than the
     * one switch that changed: the family-friendly row stands for all of
     * them, and the server takes the complete set anyway.
     */
    fun contentFilterChanges(): Observable<Set<ContentFlag>>

}

class SettingsViewImpl(
    private val fragment: PreferenceFragmentCompat
) : SettingsView {

    private val clearCacheRelay = PublishRelay.create<Unit>()
    private val contentFilterRelay = PublishRelay.create<Set<ContentFlag>>()

    private val resources = fragment.resources

    private val contentCategory: PreferenceCategory?
        get() = findPreference(R.string.pref_category_content_key)

    private val familyFriendlySwitch: SwitchPreferenceCompat?
        get() = findPreference(R.string.pref_family_friendly)

    private fun flagSwitch(flag: ContentFlag): SwitchPreferenceCompat? =
        fragment.findPreference(resources.getString(R.string.pref_content_flag_prefix) + flag.code)

    private fun <T : Preference> findPreference(keyRes: Int): T? =
        fragment.findPreference(resources.getString(keyRes))

    /**
     * Binds the switches once the preference tree exists. Every one of
     * them is non-persistent — the value lives on the server, and a copy
     * in shared preferences would be a second truth to keep in step.
     */
    fun bindContentFilter() {
        familyFriendlySwitch?.bindAsContentSwitch { checked ->
            contentFilterRelay.accept(
                if (checked) ContentFlag.entries.toSet() else emptySet()
            )
        }
        ContentFlag.entries.forEach { flag ->
            flagSwitch(flag)?.bindAsContentSwitch { checked ->
                val current = checkedFlags()
                contentFilterRelay.accept(
                    if (checked) current + flag else current - flag
                )
            }
        }
    }

    private fun SwitchPreferenceCompat.bindAsContentSwitch(onChange: (Boolean) -> Unit) {
        isPersistent = false
        setOnPreferenceChangeListener { _, newValue ->
            onChange(newValue as Boolean)
            // Let the switch move now and let the answer from the server
            // confirm it: a switch that waits for a round trip before it
            // budges reads as broken.
            true
        }
    }

    private fun checkedFlags(): Set<ContentFlag> =
        ContentFlag.entries.filterTo(mutableSetOf()) { flagSwitch(it)?.isChecked == true }

    override fun showContentFilter(flags: Set<ContentFlag>) {
        familyFriendlySwitch?.isChecked = flags.containsAll(ContentFlag.entries)
        ContentFlag.entries.forEach { flag ->
            flagSwitch(flag)?.isChecked = flag in flags
        }
        familyFriendlySwitch?.setSummary(R.string.pref_summary_family_friendly)
    }

    override fun setContentFilterEnabled(enabled: Boolean) {
        contentCategory?.isEnabled = enabled
    }

    override fun showContentFilterUnavailable() {
        contentCategory?.isEnabled = false
        familyFriendlySwitch?.setSummary(R.string.pref_summary_content_sign_in)
    }

    override fun showContentFilterError() {
        fragment.view?.let { view ->
            Snackbar.make(view, R.string.pref_content_filter_failed, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun contentFilterChanges(): Observable<Set<ContentFlag>> = contentFilterRelay

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
