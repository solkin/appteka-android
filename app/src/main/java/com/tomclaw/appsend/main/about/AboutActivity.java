package com.tomclaw.appsend.main.about;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Solkin on 17.12.2014.
 */
@EActivity(R.layout.about_activity)
public class AboutActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById
    TextView appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

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
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        finish();
        return true;
    }

    @Click(R.id.present_chocolate)
    void onChocolateClicked() {
        String donateUrl = getString(R.string.donate_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(donateUrl)));
        } catch (Throwable ignored) {
        }
    }

    @Click(R.id.feedback_email)
    void onFeedbackClicked() {
        Uri uri = Uri.fromParts("mailto", "inbox@tomclaw.com", null);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri)
                .putExtra(Intent.EXTRA_SUBJECT, "AppSend")
                .putExtra(Intent.EXTRA_TEXT, "");
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
        } catch (Throwable ex) {
            Toast.makeText(this, getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
        }
    }

    @Click(R.id.forum_discuss)
    void onForumDiscussClicked() {
        String forumUrl = getString(R.string.forum_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(forumUrl)));
        } catch (Throwable ignored) {
        }
    }

    @Click(R.id.telegram_group)
    void onTelegramGroupClicked() {
        String forumUrl = getString(R.string.telegram_group_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(forumUrl)));
        } catch (Throwable ignored) {
        }
    }
}
