package com.tomclaw.appsend.main.settings;

import static com.tomclaw.appsend.util.ThemesKt.applyTheme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.di.legacy.LegacyInjector;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private OnSettingsChangedListener listener;
    Toolbar toolbar;
    LegacyInjector legacyInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyTheme(this);
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            legacyInjector.analytics.trackEvent("open-settings-screen");
        }
    }

    // This method is called by the generated SettingsActivity_ class (e.g., from AndroidAnnotations)
    void init() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listener = new OnSettingsChangedListener();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(listener);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preferences != null && listener != null) {
            preferences.unregisterOnSharedPreferenceChangeListener(listener);
        }
    }

    boolean actionHome() {
        finish();
        return true;
    }

    public class OnSettingsChangedListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            if (TextUtils.equals(key, getString(R.string.pref_show_system))) {
                boolean show = sp.getBoolean(getString(R.string.pref_show_system),
                        getResources().getBoolean(R.bool.pref_show_system_default));
                if (show) {
                    new MaterialAlertDialogBuilder(SettingsActivity.this)
                            .setTitle(R.string.system_apps_warning_title)
                            .setMessage(R.string.system_apps_warning_message)
                            .setPositiveButton(R.string.got_it, null)
                            .show();
                }
                setResult(RESULT_OK);
            } else if (TextUtils.equals(key, getString(R.string.pref_theme_mode))) {
                // Theme mode change requires activity recreation
                recreate();
                setResult(RESULT_OK);
            } else if (TextUtils.equals(key, getString(R.string.pref_dynamic_colors))) {
                setResult(RESULT_OK);
            } else if (TextUtils.equals(key, getString(R.string.pref_sort_order))) {
                setResult(RESULT_OK);
            }
        }
    }

    public static Intent createSettingsActivityIntent(Context context) {
        return SettingsActivity_.intent(context).get();
    }
}
