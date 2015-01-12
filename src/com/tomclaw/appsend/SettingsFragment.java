package com.tomclaw.appsend;

import android.os.Bundle;
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
    }
}
