package com.tomclaw.appsend;

import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.support.v4.preference.PreferenceFragment;

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
    }
}
