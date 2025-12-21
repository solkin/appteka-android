package com.tomclaw.appsend.screen.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.tomclaw.appsend.Appteka.app
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.PleaseWaitTask
import com.tomclaw.appsend.core.TaskExecutor
import com.tomclaw.appsend.di.APPS_DIR
import com.tomclaw.appsend.util.applyTheme
import java.io.File

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<Preference>(getString(R.string.pref_theme))?.let { pref ->
            updateThemeSummary(pref)
            pref.setOnPreferenceClickListener {
                showThemeDialog()
                true
            }
        }

        findPreference<SwitchPreferenceCompat>(
            getString(R.string.pref_dynamic_colors)
        )?.let { pref ->
            pref.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            updateDynamicColorsSummary(pref)
        }

        findPreference<Preference>(getString(R.string.pref_clear_cache))
            ?.setOnPreferenceClickListener {
                showClearCacheConfirmation()
                true
            }

        findPreference<Preference>(getString(R.string.pref_sort_order))?.let { pref ->
            pref.setOnPreferenceClickListener {
                showSortDialog(pref)
                true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(
        sp: SharedPreferences,
        key: String?
    ) {
        if (key == null) return

        when (key) {
            getString(R.string.pref_theme_mode) -> {
                findPreference<Preference>(getString(R.string.pref_theme))
                    ?.let { updateThemeSummary(it) }
            }

            getString(R.string.pref_dynamic_colors) -> {
                showRestartSnackbar()
                findPreference<SwitchPreferenceCompat>(
                    getString(R.string.pref_dynamic_colors)
                )?.let { updateDynamicColorsSummary(it) }
            }
        }
    }

    private fun updateThemeSummary(pref: Preference) {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val key = getString(R.string.pref_theme_mode)

        val systemSupported = Build.VERSION.SDK_INT >= 29
        val defaultMode =
            if (systemSupported)
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            else
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY

        var mode = sp.getInt(key, defaultMode)
        if (!systemSupported && mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            mode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
        }

        val summary = when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.string.theme_light
            AppCompatDelegate.MODE_NIGHT_YES -> R.string.theme_dark
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> R.string.theme_system
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> R.string.theme_battery_saver
            else -> R.string.pref_summary_theme
        }

        pref.setSummary(summary)
    }

    private fun updateDynamicColorsSummary(pref: SwitchPreferenceCompat) {
        pref.setSummary(R.string.pref_summary_dynamic_colors)
    }

    private fun showThemeDialog() {
        val systemSupported = Build.VERSION.SDK_INT >= 29

        val options =
            if (systemSupported)
                arrayOf(
                    getString(R.string.theme_system),
                    getString(R.string.theme_light),
                    getString(R.string.theme_dark)
                )
            else
                arrayOf(
                    getString(R.string.theme_battery_saver),
                    getString(R.string.theme_light),
                    getString(R.string.theme_dark)
                )

        val modes =
            if (systemSupported)
                intArrayOf(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                    AppCompatDelegate.MODE_NIGHT_NO,
                    AppCompatDelegate.MODE_NIGHT_YES
                )
            else
                intArrayOf(
                    AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
                    AppCompatDelegate.MODE_NIGHT_NO,
                    AppCompatDelegate.MODE_NIGHT_YES
                )

        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val key = getString(R.string.pref_theme_mode)
        val current = sp.getInt(key, modes[0])

        val index = modes.indexOfFirst { it == current }.coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.theme_dialog_title)
            .setSingleChoiceItems(options, index) { d, which ->
                val selected = modes[which]
                if (selected != current) {
                    sp.edit().putInt(key, selected).apply()
                    applyTheme(requireActivity())
                }
                d.dismiss()
            }
            .show()
    }

    private fun showSortDialog(pref: Preference) {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val key = getString(R.string.pref_sort_order)

        val entries = resources.getTextArray(R.array.pref_sort_order_strings)
        val values = resources.getTextArray(R.array.pref_sort_order_values)

        val current = sp.getString(key, getString(R.string.pref_sort_order_default))
        val index = values.indexOfFirst { TextUtils.equals(it, current) }.coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(pref.title)
            .setSingleChoiceItems(entries, index) { d, which ->
                sp.edit().putString(key, values[which].toString()).apply()
                requireActivity().setResult(AppCompatActivity.RESULT_OK)
                d.dismiss()
            }
            .show()
    }

    private fun showRestartSnackbar() {
        val v = view ?: return

        Snackbar.make(v, R.string.restart_required_dynamic_colors_short, Snackbar.LENGTH_LONG)
            .setAction(R.string.restart_now) {
                val ctx = context ?: return@setAction
                val i = ctx.packageManager
                    .getLaunchIntentForPackage(ctx.packageName)
                i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                ctx.startActivity(i)
                Runtime.getRuntime().exit(0)
            }
            .show()
    }

    private fun showClearCacheConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_cache_dialog_title)
            .setMessage(getString(R.string.clear_cache_dialog_message, "AppTeka"))
            .setPositiveButton(R.string.clear_cache_dialog_positive) { _, _ ->
                clearCache()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun clearCache() {
        TaskExecutor.getInstance().execute(
            object : PleaseWaitTask(activity) {

                override fun executeBackground() {
                    val dir = File(app().cacheDir, APPS_DIR)
                    dir.listFiles { f -> f.name.endsWith(".apk") }?.forEach { it.delete() }
                }

                override fun onSuccessMain() {
                    Toast.makeText(
                        getWeakObject(),
                        R.string.cache_cleared_successfully,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailMain(ex: Throwable) {
                    Toast.makeText(
                        getWeakObject(),
                        R.string.cache_clearing_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}