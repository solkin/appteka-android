package com.tomclaw.appsend;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Solkin on 17.12.2014.
 */
public class AboutActivity extends Activity {

    private static final String MARKET_DETAILS_URI = "market://details?id=";
    private static final String MARKET_DEVELOPER_URI = "market://search?q=";
    private static final String GOOGLE_PLAY_DETAILS_URI = "http://play.google.com/store/apps/details?id=";
    private static final String GOOGLE_PLAY_DEVELOPER_URI = "http://play.google.com/store/apps/search?q=";
    public static String DEVELOPER_NAME = "TomClaw Software";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_activity);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        TextView appVersionView = (TextView) findViewById(R.id.app_version);
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            appVersionView.setText(getString(R.string.app_version, info.versionName, info.versionCode));
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        findViewById(R.id.rate_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateApplication();
            }
        });

        findViewById(R.id.all_apps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allProjects();
            }
        });
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

    private void rateApplication() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(MARKET_DETAILS_URI + appPackageName)));
        } catch (android.content.ActivityNotFoundException ignored) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(GOOGLE_PLAY_DETAILS_URI + appPackageName)));
        }
    }

    private void allProjects() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(MARKET_DEVELOPER_URI + DEVELOPER_NAME)));
        } catch (android.content.ActivityNotFoundException ignored) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(GOOGLE_PLAY_DEVELOPER_URI + DEVELOPER_NAME)));
        }
    }
}
