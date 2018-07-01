package com.tomclaw.appsend.main.local;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tomclaw.appsend.R;

public class LocalAppsFragment extends Fragment {

    public static final String ARG_OBJECT = "object";

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.local_apps_container, container, false);
        Bundle args = getArguments();
        ((TextView) rootView.findViewById(R.id.text)).setText(
                Integer.toString(args.getInt(ARG_OBJECT)));
        return rootView;
    }

}
