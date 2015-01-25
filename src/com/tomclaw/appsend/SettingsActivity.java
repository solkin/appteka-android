package com.tomclaw.appsend;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 9/30/13
 * Time: 7:37 PM
 */
public class SettingsActivity extends ActionBarActivity {

    public static final int RESULT_UPDATE = 5;
    private SharedPreferences preferences;
    private OnSettingsChangedListener listener;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_activity);

        listener = new OnSettingsChangedListener();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(listener);
        settingsFragment = new SettingsFragment();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, settingsFragment)
                .commit();

        Utils.setupTint(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
        }
        return true;
    }

    public class OnSettingsChangedListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Context context = SettingsActivity.this;
            // Checking for preference changed.
            if (TextUtils.equals(key, getString(R.string.pref_show_system))) {
                if (sharedPreferences.getBoolean(context.getString(R.string.pref_show_system),
                        context.getResources().getBoolean(R.bool.pref_show_system_default))) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.system_apps_warning_title)
                            .setMessage(R.string.system_apps_warning_message)
                            .setNeutralButton(R.string.got_it, null).create();
                    alertDialog.show();
                }
                setResult(RESULT_UPDATE);
            } else if (TextUtils.equals(key, getString(R.string.pref_sort_order))) {
                setResult(RESULT_UPDATE);
            }
        }
    }
}
