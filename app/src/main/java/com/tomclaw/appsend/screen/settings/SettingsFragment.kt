package com.tomclaw.appsend.screen.settings

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.core.ProxyAddressParser
import com.tomclaw.appsend.core.ProxyCheckResult
import com.tomclaw.appsend.core.ProxyChecker
import com.tomclaw.appsend.core.ProxyConfig
import com.tomclaw.appsend.core.ProxyConfigProvider
import com.tomclaw.appsend.core.ProxyType
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.screen.settings.di.SettingsModule
import com.tomclaw.appsend.util.ThemeManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(),
    SettingsPresenter.SettingsRouter,
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var presenter: SettingsPresenter

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var apkStorage: ApkStorage

    @Inject
    lateinit var proxyConfigProvider: ProxyConfigProvider

    @Inject
    lateinit var proxyChecker: ProxyChecker

    private lateinit var settingsView: SettingsView

    private var pendingStoragePermissionCallback: ((Boolean) -> Unit)? = null

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        pendingStoragePermissionCallback?.invoke(granted)
        pendingStoragePermissionCallback = null
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        requireContext().appComponent
            .settingsComponent(SettingsModule(requireContext(), presenterState))
            .inject(fragment = this)

        setPreferencesFromResource(R.xml.preferences, rootKey)

        settingsView = SettingsViewImpl(this)

        setupThemePreference()
        setupDynamicColorsPreference()
        setupSeedColorPreference()
        setupSortOrderPreference()
        setupClearCachePreference()
        setupProxyPreferences()
    }

    private fun setupThemePreference() {
        findPreference<Preference>(getString(R.string.pref_theme))?.let { pref ->
            updateThemeSummary(pref)
            pref.setOnPreferenceClickListener {
                showThemeDialog()
                true
            }
        }
    }

    private fun setupDynamicColorsPreference() {
        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_dynamic_colors))?.let { pref ->
            pref.isEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            updateDynamicColorsSummary(pref)
        }
    }

    private fun setupSeedColorPreference() {
        findPreference<Preference>(getString(R.string.pref_seed_color))?.let { pref ->
            updateSeedColorState(pref)
            pref.setOnPreferenceClickListener {
                showSeedColorPicker()
                true
            }
        }
    }

    private fun updateSeedColorState(pref: Preference) {
        val dynamicEnabled = themeManager.isDynamicColorsEnabled()
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        pref.isEnabled = !dynamicEnabled
    }

    private fun setupSortOrderPreference() {
        findPreference<Preference>(getString(R.string.pref_sort_order))?.let { pref ->
            pref.setOnPreferenceClickListener {
                showSortDialog(pref)
                true
            }
        }
    }

    private fun setupClearCachePreference() {
        findPreference<Preference>(getString(R.string.pref_clear_cache))?.setOnPreferenceClickListener {
            (settingsView as SettingsViewImpl).onClearCacheClick()
            true
        }
    }

    private fun setupProxyPreferences() {
        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_proxy_enabled))?.let { pref ->
            val config = proxyConfigProvider.getProxyConfig()
            pref.isChecked = config.enabled
            pref.setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                val currentConfig = proxyConfigProvider.getProxyConfig()
                proxyConfigProvider.setProxyConfig(currentConfig.copy(enabled = enabled))
                updateProxySettingsSummary()
                true
            }
        }

        findPreference<Preference>(getString(R.string.pref_proxy_settings))?.let { pref ->
            updateProxySettingsSummary()
            pref.setOnPreferenceClickListener {
                showProxySettingsDialog()
                true
            }
        }
    }

    private fun updateProxySettingsSummary() {
        findPreference<Preference>(getString(R.string.pref_proxy_settings))?.let { pref ->
            val config = proxyConfigProvider.getProxyConfig()
            pref.summary = if (config.host.isNotBlank() && config.port > 0) {
                "${config.type.name}: ${config.host}:${config.port}"
            } else {
                getString(R.string.pref_summary_proxy_settings)
            }
        }
    }

    private fun showProxySettingsDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_proxy_settings, null)

        val hostInput = dialogView.findViewById<EditText>(R.id.proxy_host_input)
        val portInput = dialogView.findViewById<EditText>(R.id.proxy_port_input)
        val typeGroup = dialogView.findViewById<RadioGroup>(R.id.proxy_type_group)
        val checkButton = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.proxy_check_button)
        val checkProgress = dialogView.findViewById<ProgressBar>(R.id.proxy_check_progress)
        val checkResult = dialogView.findViewById<TextView>(R.id.proxy_check_result)

        var checkDisposable: Disposable? = null

        val config = proxyConfigProvider.getProxyConfig()
        hostInput.setText(config.host)
        if (config.port > 0) {
            portInput.setText(config.port.toString())
        }
        typeGroup.check(
            when (config.type) {
                ProxyType.SOCKS -> R.id.proxy_type_socks
                else -> R.id.proxy_type_http
            }
        )

        // Add TextWatcher to parse proxy address on paste
        var isUpdating = false
        hostInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val text = s?.toString() ?: return

                val parsed = ProxyAddressParser.parse(text) ?: return

                isUpdating = true
                hostInput.setText(parsed.host)
                hostInput.setSelection(parsed.host.length)

                parsed.port?.let { port ->
                    portInput.setText(port.toString())
                }

                parsed.type?.let { type ->
                    typeGroup.check(
                        when (type) {
                            ProxyType.SOCKS -> R.id.proxy_type_socks
                            ProxyType.HTTP -> R.id.proxy_type_http
                        }
                    )
                }
                isUpdating = false
            }
        })

        checkButton.setOnClickListener {
            val host = hostInput.text.toString().trim()
            val portText = portInput.text.toString().trim()
            val port = portText.toIntOrNull() ?: 0
            val type = when (typeGroup.checkedRadioButtonId) {
                R.id.proxy_type_socks -> ProxyType.SOCKS
                else -> ProxyType.HTTP
            }

            val testConfig = ProxyConfig(
                enabled = true,
                host = host,
                port = port,
                type = type
            )

            if (!testConfig.isValid() || host.isBlank() || port <= 0) {
                checkResult.visibility = View.VISIBLE
                checkResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                checkResult.text = getString(R.string.proxy_settings_invalid)
                return@setOnClickListener
            }

            checkDisposable?.dispose()
            checkButton.isEnabled = false
            checkProgress.visibility = View.VISIBLE
            checkResult.visibility = View.GONE

            checkDisposable = proxyChecker.check(testConfig)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { result ->
                    checkButton.isEnabled = true
                    checkProgress.visibility = View.GONE
                    checkResult.visibility = View.VISIBLE

                    when (result) {
                        is ProxyCheckResult.Success -> {
                            checkResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                            checkResult.text = getString(R.string.proxy_check_success)
                        }
                        is ProxyCheckResult.Error -> {
                            checkResult.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                            checkResult.text = getString(R.string.proxy_check_error, result.message)
                        }
                    }
                }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.proxy_settings_dialog_title)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { _, _ ->
                val host = hostInput.text.toString().trim()
                val portText = portInput.text.toString().trim()
                val port = portText.toIntOrNull() ?: 0
                val type = when (typeGroup.checkedRadioButtonId) {
                    R.id.proxy_type_socks -> ProxyType.SOCKS
                    else -> ProxyType.HTTP
                }

                val newConfig = proxyConfigProvider.getProxyConfig().copy(
                    host = host,
                    port = port,
                    type = type
                )

                if (newConfig.isValid()) {
                    proxyConfigProvider.setProxyConfig(newConfig)
                    updateProxySettingsSummary()
                } else {
                    Snackbar.make(
                        requireView(),
                        R.string.proxy_settings_invalid,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setOnDismissListener {
                checkDisposable?.dispose()
            }
            .show()
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

    override fun onStart() {
        super.onStart()
        presenter.attachView(settingsView)
        presenter.attachRouter(this)
    }

    override fun onStop() {
        presenter.detachRouter()
        presenter.detachView()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_PRESENTER_STATE, presenter.saveState())
    }

    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String?) {
        if (key == null) return

        when (key) {
            getString(R.string.pref_theme_mode) -> {
                findPreference<Preference>(getString(R.string.pref_theme))
                    ?.let { updateThemeSummary(it) }
            }

            getString(R.string.pref_dynamic_colors) -> {
                findPreference<SwitchPreferenceCompat>(
                    getString(R.string.pref_dynamic_colors)
                )?.let { updateDynamicColorsSummary(it) }
                findPreference<Preference>(getString(R.string.pref_seed_color))
                    ?.let { updateSeedColorState(it) }
                restartActivity()
            }
        }
    }

    private fun updateThemeSummary(pref: Preference) {
        val sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val key = getString(R.string.pref_theme_mode)

        val systemSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
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
        val systemSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

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
                    themeManager.applyNightMode()
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

    private fun showSeedColorPicker() {
        val seedColors = SEED_COLORS
        val currentColor = themeManager.getSeedColor()

        val dp = resources.displayMetrics.density
        val circleSize = (48 * dp).toInt()
        val margin = (8 * dp).toInt()
        val padding = (24 * dp).toInt()

        val grid = GridLayout(requireContext()).apply {
            columnCount = 4
            setPadding(padding, padding, padding, padding)
        }

        var dialog: androidx.appcompat.app.AlertDialog? = null

        for (color in seedColors) {
            val circle = View(requireContext()).apply {
                val drawable = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    if (color == currentColor) {
                        setStroke((3 * dp).toInt(), getContrastColor(color))
                    }
                }
                background = drawable
                if (color == currentColor) {
                    foreground = ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_check_circle
                    )?.apply {
                        setTint(getContrastColor(color))
                    }
                    foregroundGravity = Gravity.CENTER
                }
            }
            val params = GridLayout.LayoutParams().apply {
                width = circleSize
                height = circleSize
                setMargins(margin, margin, margin, margin)
            }
            circle.layoutParams = params
            circle.setOnClickListener {
                themeManager.setSeedColor(color)
                dialog?.dismiss()
                restartActivity()
            }
            grid.addView(circle)
        }

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.seed_color_dialog_title)
            .setView(grid)
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun getContrastColor(color: Int): Int {
        val luminance = (0.299 * Color.red(color)
                + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color)) / 255
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    override fun finishActivity() {
        requireActivity().finish()
    }

    override fun restartActivity() {
        val intent = requireActivity().intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        requireActivity().finish()
        @Suppress("DEPRECATION")
        requireActivity().overridePendingTransition(0, 0)
        startActivity(intent)
    }

    override fun setResultOk() {
        requireActivity().setResult(android.app.Activity.RESULT_OK)
    }

    override fun requestStoragePermissions(callback: (Boolean) -> Unit) {
        if (!apkStorage.isPermissionRequired()) {
            callback(true)
            return
        }
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                callback(true)
            }

            shouldShowRequestPermissionRationale(permission) -> {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.write_permission_clear_cache)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        pendingStoragePermissionCallback = callback
                        storagePermissionLauncher.launch(permission)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        callback(false)
                    }
                    .show()
            }

            else -> {
                pendingStoragePermissionCallback = callback
                storagePermissionLauncher.launch(permission)
            }
        }
    }

    companion object {
        private val SEED_COLORS = intArrayOf(
            0xFF32A304.toInt(),
            0xFF4285F4.toInt(),
            0xFFEA4335.toInt(),
            0xFFFBBC04.toInt(),
            0xFF34A853.toInt(),
            0xFFFF6D00.toInt(),
            0xFF9C27B0.toInt(),
            0xFF00BCD4.toInt(),
            0xFF795548.toInt(),
            0xFFE91E63.toInt(),
            0xFF3F51B5.toInt(),
            0xFF607D8B.toInt(),
        )
    }

}

private const val KEY_PRESENTER_STATE = "presenter_state"
