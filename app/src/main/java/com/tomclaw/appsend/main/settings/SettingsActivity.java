package com.tomclaw.appsend.main.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import static com.tomclaw.appsend.util.Analytics.trackEvent;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 9/30/13
 * Time: 7:37 PM
 */
@EActivity(R.layout.settings_activity)
public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private OnSettingsChangedListener listener;

    @ViewById
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            trackEvent("open-settings-screen");
        }
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

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
        preferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        finish();
        return true;
    }

    public class OnSettingsChangedListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Context context = SettingsActivity.this;
            if (TextUtils.equals(key, getString(R.string.pref_show_system))) {
                if (sharedPreferences.getBoolean(context.getString(R.string.pref_show_system),
                        context.getResources().getBoolean(R.bool.pref_show_system_default))) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(context)
                            .setTitle(R.string.system_apps_warning_title)
                            .setMessage(R.string.system_apps_warning_message)
                            .setNeutralButton(R.string.got_it, null).create();
                    alertDialog.show();
                }
                setResult(RESULT_OK);
            } else if (TextUtils.equals(key, getString(R.string.pref_dark_theme))) {
                Intent intent = getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
            } else if (TextUtils.equals(key, getString(R.string.pref_sort_order))) {
                setResult(RESULT_OK);
            }
        }

    }

}
