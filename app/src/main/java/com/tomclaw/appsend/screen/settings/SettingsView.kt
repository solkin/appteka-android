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

    /**
     * Paints the content filter from the set the account hides. The
     * switches say what is *shown*, so the set is inverted here rather
     * than on the wire — the server, the website and this screen then
     * all agree on what an untouched account means: nothing hidden.
     */
    fun showContentFilter(flags: Set<ContentFlag>)

    /**
     * Greys the block out while the value is in flight or when there is
     * no account to hold it, with a summary saying which of the two.
     */
    fun setContentFilterEnabled(enabled: Boolean)

    fun showContentFilterUnavailable()

    fun showContentFilterError()

    /** A category switch was moved, and whether it is now shown. */
    fun contentFlagChanges(): Observable<ContentFlagChange>

    /**
     * The family-friendly switch was moved. It stands for the whole
     * list rather than a flag of its own, so what it means is the
     * presenter's to decide.
     */
    fun familyFriendlyChanges(): Observable<Boolean>

}

data class ContentFlagChange(
    val flag: ContentFlag,
    val shown: Boolean,
)

class SettingsViewImpl(
    private val fragment: PreferenceFragmentCompat
) : SettingsView {

    private val clearCacheRelay = PublishRelay.create<Unit>()
    private val contentFlagRelay = PublishRelay.create<ContentFlagChange>()
    private val familyFriendlyRelay = PublishRelay.create<Boolean>()

    private val resources = fragment.resources

    // The row lives on the settings screen, the switches on the one it
    // opens, and each is inflated on its own. Every lookup is therefore
    // allowed to come back empty — whichever screen is on show updates
    // the part of the filter it holds.
    private val contentFilterRow: Preference?
        get() = findPreference(R.string.pref_content_filter_key)

    private val contentFilterGroup: PreferenceCategory?
        get() = findPreference(R.string.pref_content_filter_group_key)

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
            familyFriendlyRelay.accept(checked)
        }
        ContentFlag.entries.forEach { flag ->
            flagSwitch(flag)?.bindAsContentSwitch { shown ->
                contentFlagRelay.accept(ContentFlagChange(flag, shown))
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

    override fun showContentFilter(flags: Set<ContentFlag>) {
        familyFriendlySwitch?.isChecked = flags.containsAll(ContentFlag.entries)
        familyFriendlySwitch?.setSummary(R.string.pref_summary_family_friendly)
        ContentFlag.entries.forEach { flag ->
            flagSwitch(flag)?.apply {
                isChecked = flag !in flags
                summary = flagSummary(flag, hidden = flag in flags)
            }
        }
        contentFilterRow?.summary = filterSummary(flags)
    }

    /**
     * "Shown · Casinos, betting, slots, lotteries". The switch already
     * carries the state, but only the wording says which way it points,
     * and a row scrolled past its category header has nothing else.
     */
    private fun flagSummary(flag: ContentFlag, hidden: Boolean): String {
        val state = resources.getString(
            if (hidden) R.string.content_state_hidden else R.string.content_state_shown
        )
        return state + SUMMARY_SEPARATOR + resources.getString(flag.summaryRes)
    }

    /** What the settings row says without being opened. */
    private fun filterSummary(flags: Set<ContentFlag>): String {
        if (flags.isEmpty()) {
            return resources.getString(R.string.pref_summary_content_nothing_hidden)
        }
        val names = ContentFlag.entries
            .filter { it in flags }
            .joinToString { resources.getString(it.titleRes) }
        return resources.getString(R.string.content_filter_active, names)
    }

    override fun setContentFilterEnabled(enabled: Boolean) {
        contentFilterRow?.isEnabled = enabled
        contentFilterGroup?.isEnabled = enabled
    }

    override fun showContentFilterUnavailable() {
        contentFilterRow?.isEnabled = false
        contentFilterRow?.setSummary(R.string.pref_summary_content_sign_in)
        contentFilterGroup?.isEnabled = false
        familyFriendlySwitch?.setSummary(R.string.pref_summary_content_sign_in)
    }

    override fun showContentFilterError() {
        fragment.view?.let { view ->
            Snackbar.make(view, R.string.pref_content_filter_failed, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun contentFlagChanges(): Observable<ContentFlagChange> = contentFlagRelay

    override fun familyFriendlyChanges(): Observable<Boolean> = familyFriendlyRelay

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

private const val SUMMARY_SEPARATOR = " \u00b7 "
