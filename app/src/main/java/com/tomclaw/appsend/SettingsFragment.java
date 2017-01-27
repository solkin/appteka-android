package com.tomclaw.appsend;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.widget.Toast;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.tomclaw.appsend.core.PleaseWaitTask;
import com.tomclaw.appsend.core.TaskExecutor;

import java.io.File;
import java.io.FileFilter;

import static com.tomclaw.appsend.util.FileHelper.getExternalDirectory;

/**
 * Created by Solkin on 12.01.2015.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            ListPreference preference = (ListPreference) findPreference(getString(R.string.pref_sort_order));
            preference.setEntries(R.array.pref_sort_order_strings_legacy);
            preference.setEntryValues(R.array.pref_sort_order_values_legacy);
        }
        Preference myPref = (Preference) findPreference(getString(R.string.pref_clear_cache));
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                TaskExecutor.getInstance().execute(new PleaseWaitTask(getActivity()) {
                    @Override
                    public void executeBackground() throws Throwable {
                        File directory = getExternalDirectory();
                        File[] files = directory.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                return pathname.getName().endsWith(".apk");
                            }
                        });
                        for (File file : files) {
                            file.delete();
                        }
                    }

                    @Override
                    public void onSuccessMain() {
                        Context context = getWeakObject();
                        if (context != null) {
                            Toast.makeText(context, R.string.cache_cleared_successfully, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailMain(Throwable ex) {
                        Context context = getWeakObject();
                        if (context != null) {
                            Toast.makeText(context, R.string.cache_clearing_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                return true;
            }
        });
    }
}
