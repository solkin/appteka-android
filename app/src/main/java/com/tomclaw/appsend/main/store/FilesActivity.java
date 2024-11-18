package com.tomclaw.appsend.main.store;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.util.ThemeHelper;

/**
 * Created by Igor on 22.10.2017.
 */
@SuppressLint("Registered")
public class FilesActivity extends AppCompatActivity {

    Toolbar toolbar;

    UploadsFragment uploadsFragment;

    Long userId;

    LegacyInjector legacyInjector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            legacyInjector.analytics.trackEvent("open-files-screen");
        }
    }

    void init() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        uploadsFragment.setUserId(userId);
    }

    boolean actionHome() {
        onBackPressed();
        return true;
    }

    public static Intent createUserAppsActivityIntent(Context context, int userId) {
        return FilesActivity_.intent(context).userId((long) userId).get();
    }

}
