package com.tomclaw.appsend;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 9/30/13
 * Time: 7:37 PM
 */
public class SettingsActivity extends Activity {

    public static final int RESULT_UPDATE = 5;
    private SharedPreferences preferences;
    private OnSettingsChangedListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listener = new OnSettingsChangedListener();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(listener);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
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

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource.
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    public class OnSettingsChangedListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Context context = SettingsActivity.this;
            // Checking for preference changed.
            if (TextUtils.equals(key, getString(R.string.pref_show_system))) {
                if(sharedPreferences.getBoolean(context.getString(R.string.pref_show_system),
                        context.getResources().getBoolean(R.bool.pref_show_system_default))) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.system_apps_warning_title)
                            .setMessage(R.string.system_apps_warning_message)
                            .setNeutralButton(R.string.got_it, null).create();
                    alertDialog.show();
                }
                setResult(RESULT_UPDATE);
            }
        }
    }
}
