package com.tomclaw.appsend.main.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.EActivity;

/**
 * Created by solkin on 16/03/2018.
 */
@EActivity(R.layout.profile_activity)
public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

}
