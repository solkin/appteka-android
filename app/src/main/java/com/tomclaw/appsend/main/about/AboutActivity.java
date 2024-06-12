package com.tomclaw.appsend.main.about;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

/**
 * Created by Solkin on 17.12.2014.
 */
@EActivity
public class AboutActivity extends AppCompatActivity {

    @Bean
    LegacyInjector injector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView appVersion = findViewById(R.id.app_version);

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

        findViewById(R.id.feedback_email).setOnClickListener(v -> onFeedbackClicked());
        findViewById(R.id.forum_discuss).setOnClickListener(v -> onForumDiscussClicked());
        findViewById(R.id.telegram_group).setOnClickListener(v -> onTelegramGroupClicked());
        findViewById(R.id.legal_info).setOnClickListener(v -> onLegalInfoClicked());

        if (savedInstanceState == null) {
            injector.analytics.trackEvent("open-about-screen");
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
        injector.analytics.trackEvent("click-email-feedback");
    }

    private void onForumDiscussClicked() {
        String url = getString(R.string.forum_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Throwable ignored) {
        }
        injector.analytics.trackEvent("click-4pda-forum");
    }

    private void onTelegramGroupClicked() {
        String url = getString(R.string.telegram_group_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Throwable ignored) {
        }
        injector.analytics.trackEvent("click-telegram-group");
    }

    private void onLegalInfoClicked() {
        String url = getString(R.string.legal_info_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Throwable ignored) {
        }
        injector.analytics.trackEvent("click-legal-info");
    }

    public static Intent createAboutActivityIntent(Context context) {
        return AboutActivity_.intent(context).get();
    }

}
