package com.tomclaw.appsend.main.settings;

import static com.tomclaw.appsend.Appteka.app;
import static com.tomclaw.appsend.di.AppModuleKt.APPS_DIR;
import static com.tomclaw.appsend.util.ThemesKt.applyTheme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.PleaseWaitTask;
import com.tomclaw.appsend.core.TaskExecutor;

import java.io.File;

/** Fragment for displaying and handling app settings. */
public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /** Called to create and load preferences from XML. */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference themePref = findPreference(getString(R.string.pref_theme));
        if (themePref != null) {
            updateThemeSummary(themePref);
            themePref.setOnPreferenceClickListener(
                    p -> {
                        showThemeDialog();
                        return true;
                    });
        }

        SwitchPreferenceCompat dynamicPref =
                findPreference(getString(R.string.pref_dynamic_colors));
        if (dynamicPref != null) {
            dynamicPref.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);
            updateDynamicColorsSummary(dynamicPref);

            dynamicPref.setOnPreferenceChangeListener(
                    (p, newValue) -> {
                        return true;
                    });
        }

        Preference clearPref = findPreference(getString(R.string.pref_clear_cache));
        if (clearPref != null) {
            clearPref.setOnPreferenceClickListener(
                    p -> {
                        showClearCacheConfirmation();
                        return true;
                    });
        }

        Preference sortPref = findPreference(getString(R.string.pref_sort_order));
        if (sortPref != null) {
            sortPref.setOnPreferenceClickListener(
                    p -> {
                        showSortDialog(sortPref);
                        return true;
                    });
        }
    }

    /** Registers the preference change listener. */
    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    /** Unregisters the preference change listener. */
    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /** Handles changes to shared preferences, primarily for theme and dynamic colors. */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (TextUtils.equals(key, getString(R.string.pref_theme_mode))) {
            Preference themePref = findPreference(getString(R.string.pref_theme));
            if (themePref != null) {
                updateThemeSummary(themePref);
            }
        } else if (TextUtils.equals(key, getString(R.string.pref_dynamic_colors))) {
            showRestartSnackbar();
            SwitchPreferenceCompat dynamicPref =
                    findPreference(getString(R.string.pref_dynamic_colors));
            if (dynamicPref != null) {
                updateDynamicColorsSummary(dynamicPref);
            }
        }
    }

    /** Updates the summary of the theme preference based on the selected mode. */
    private void updateThemeSummary(Preference themePref) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String key = getString(R.string.pref_theme_mode);

        boolean systemSupported = Build.VERSION.SDK_INT >= 29;
        int defaultMode =
                systemSupported
                        ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        : AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
        int currentMode = pref.getInt(key, defaultMode);

        if (!systemSupported && currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            currentMode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
        }

        int summaryResId;
        switch (currentMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                summaryResId = R.string.theme_light;
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                summaryResId = R.string.theme_dark;
                break;
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                summaryResId = R.string.theme_system;
                break;
            case AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY:
                summaryResId = R.string.theme_battery_saver;
                break;
            default:
                summaryResId = R.string.pref_summary_theme; // Fallback
        }
        themePref.setSummary(getString(summaryResId));
    }

    /** Sets a fixed summary for dynamic colors preference. */
    private void updateDynamicColorsSummary(SwitchPreferenceCompat dynamicPref) {
        dynamicPref.setSummary(R.string.pref_summary_dynamic_colors);
    }

    /** Shows a single-choice Material Dialog for selecting the app theme. */
    private void showThemeDialog() {
        boolean systemSupported = Build.VERSION.SDK_INT >= 29;

        String[] options =
                systemSupported
                        ? new String[] {
                            getString(R.string.theme_system),
                            getString(R.string.theme_light),
                            getString(R.string.theme_dark)
                        }
                        : new String[] {
                            getString(R.string.theme_battery_saver),
                            getString(R.string.theme_light),
                            getString(R.string.theme_dark)
                        };

        final int[] modes =
                systemSupported
                        ? new int[] {
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                            AppCompatDelegate.MODE_NIGHT_NO,
                            AppCompatDelegate.MODE_NIGHT_YES
                        }
                        : new int[] {
                            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY,
                            AppCompatDelegate.MODE_NIGHT_NO,
                            AppCompatDelegate.MODE_NIGHT_YES
                        };

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String key = getString(R.string.pref_theme_mode);
        int defaultMode =
                systemSupported
                        ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        : AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
        int currentMode = pref.getInt(key, defaultMode);

        if (!systemSupported && currentMode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) {
            currentMode = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
        }

        final int finalCurrentMode = currentMode;

        int selectedIndex = 0;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.theme_dialog_title)
                .setSingleChoiceItems(
                        options,
                        selectedIndex,
                        (dialog, which) -> {
                            int selectedMode = modes[which];
                            if (selectedMode != finalCurrentMode) {
                                pref.edit().putInt(key, selectedMode).apply();
                                applyTheme(requireActivity());
                            }
                            dialog.dismiss();
                        })
                .show();
    }

    /** Shows a custom Material AlertDialog for selecting the list sort order. */
    private void showSortDialog(Preference sortPref) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(requireContext());
        String key = getString(R.string.pref_sort_order);

        final CharSequence[] entries = getResources().getTextArray(R.array.pref_sort_order_strings);
        final CharSequence[] entryValues =
                getResources().getTextArray(R.array.pref_sort_order_values);

        String currentValue = pref.getString(key, getString(R.string.pref_sort_order_default));
        int selectedIndex = 0;
        for (int i = 0; i < entryValues.length; i++) {
            if (TextUtils.equals(entryValues[i], currentValue)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(sortPref.getTitle())
                .setSingleChoiceItems(
                        entries,
                        selectedIndex,
                        (dialog, which) -> {
                            String newValue = entryValues[which].toString();

                            pref.edit().putString(key, newValue).apply();

                            requireActivity().setResult(requireActivity().RESULT_OK);

                            dialog.dismiss();
                        })
                .show();
    }

    /** Shows a themed Snackbar requesting the user to restart the app. */
    private void showRestartSnackbar() {
    View view = getView();
    if (view == null) return;

    Snackbar snackbar = Snackbar.make(view, R.string.restart_required_dynamic_colors_short, Snackbar.LENGTH_LONG)
            .setAction(R.string.restart_now, v -> {
                try {
                    Context ctx = getContext();
                    if (ctx != null) {
                        Intent i = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
                        if (i != null) {
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            ctx.startActivity(i);
                            Runtime.getRuntime().exit(0);
                        }
                    }
                } catch (Throwable ignored) {}
            });

    Context context = requireActivity();
    TypedValue tv = new TypedValue();

    // Action button → primary color (App theme attribute)
    context.getTheme().resolveAttribute(R.attr.colorPrimary, tv, true);
    int primaryColor = tv.data;

    // Snackbar container color → M3 elevated container
    context.getTheme().resolveAttribute(
            com.google.android.material.R.attr.colorSurfaceContainerHigh,
            tv, true
    );
    int surfaceContainerColor = tv.data;

    // Snackbar text color → onSurface
    context.getTheme().resolveAttribute(
            com.google.android.material.R.attr.colorOnSurface,
            tv, true
    );
    int onSurfaceColor = tv.data;

    snackbar.getView().setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(surfaceContainerColor)
    );
    snackbar.setActionTextColor(primaryColor);

    TextView snackbarText =
            snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
    if (snackbarText != null) {
        snackbarText.setTextColor(onSurfaceColor);
    }

    snackbar.show();
    }

    /** Shows a confirmation dialog before clearing the app's downloaded APK cache. */
    private void showClearCacheConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_cache_dialog_title)
                .setMessage(getString(R.string.clear_cache_dialog_message, "AppTeka"))
                .setPositiveButton(
                        R.string.clear_cache_dialog_positive, (dialog, which) -> clearCache())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Clears the cached APK files in a background thread. */
    private void clearCache() {
        TaskExecutor.getInstance()
                .execute(
                        new PleaseWaitTask(getActivity()) {
                            @Override
                            public void executeBackground() throws Throwable {
                                File dir = new File(app().getCacheDir(), APPS_DIR);
                                File[] files = dir.listFiles(f -> f.getName().endsWith(".apk"));
                                if (files != null) {
                                    for (File f : files) {
                                        f.delete();
                                    }
                                }
                            }

                            @Override
                            public void onSuccessMain() {
                                Context c = getWeakObject();
                                if (c != null) {
                                    Toast.makeText(
                                                    c,
                                                    R.string.cache_cleared_successfully,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }

                            @Override
                            public void onFailMain(Throwable ex) {
                                Context c = getWeakObject();
                                if (c != null) {
                                    Toast.makeText(
                                                    c,
                                                    R.string.cache_clearing_failed,
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
    }
}
