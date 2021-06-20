package com.tomclaw.appsend.main.about;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.ThemeHelper;

/**
 * Created by Solkin on 17.12.2014.
 */
public class AboutActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_activity);

        toolbar = findViewById(R.id.toolbar);
        appVersion = findViewById(R.id.app_version);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            appVersion.setText(getString(R.string.app_version, info.versionName, info.versionCode));
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        findViewById(R.id.present_chocolate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChocolateClicked();
            }
        });
        findViewById(R.id.feedback_email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFeedbackClicked();
            }
        });
        findViewById(R.id.forum_discuss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onForumDiscussClicked();
            }
        });
        findViewById(R.id.telegram_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTelegramGroupClicked();
            }
        });

        if (savedInstanceState == null) {
            trackEvent("open-about-screen");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onChocolateClicked() {
        String donateUrl = getString(R.string.donate_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(donateUrl)));
        } catch (Throwable ignored) {
        }
        trackEvent("click-donate");
    }

    private void onFeedbackClicked() {
        Uri uri = Uri.fromParts("mailto", "inbox@tomclaw.com", null);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri)
                .putExtra(Intent.EXTRA_SUBJECT, "Appteka")
                .putExtra(Intent.EXTRA_TEXT, "");
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
        } catch (Throwable ex) {
            Toast.makeText(this, getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
        }
        trackEvent("click-email-feedback");
    }

    private void onForumDiscussClicked() {
        String forumUrl = getString(R.string.forum_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(forumUrl)));
        } catch (Throwable ignored) {
        }
        trackEvent("click-4pda-forum");
    }

    private void onTelegramGroupClicked() {
        String forumUrl = getString(R.string.telegram_group_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(forumUrl)));
        } catch (Throwable ignored) {
        }
        trackEvent("click-telegram-group");
    }
}
