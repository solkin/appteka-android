package com.tomclaw.appsend;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.tomclaw.appsend.util.ThemeHelper;

/**
 * Created by Solkin on 17.12.2014.
 */
public class AboutActivity extends AppCompatActivity {

    private View presentButton;
    private View feedbackButton;
    private View forumDiscuss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_activity);
        ThemeHelper.updateStatusBar(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        TextView appVersionView = (TextView) findViewById(R.id.app_version);
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            appVersionView.setText(getString(R.string.app_version, info.versionName, info.versionCode));
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        presentButton = findViewById(R.id.present_chocolate);
        presentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChocolateClicked();
            }
        });

        feedbackButton = findViewById(R.id.feedback_email);
        feedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFeedbackClicked();
            }
        });

        forumDiscuss = findViewById(R.id.forum_discuss);
        forumDiscuss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onForumDiscussClicked();
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

    private void onChocolateClicked() {
        String donateUrl = getString(R.string.donate_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(donateUrl)));
        } catch (Throwable ignored) {
        }
    }

    private void onFeedbackClicked() {
        Uri uri = Uri.fromParts("mailto","inbox@tomclaw.com", null);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri)
                .putExtra(Intent.EXTRA_SUBJECT, "AppSend")
                .putExtra(Intent.EXTRA_TEXT, "");
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
        } catch (Throwable ex) {
            Toast.makeText(this, getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show();
        }
    }

    private void onForumDiscussClicked() {
        String forumUrl = getString(R.string.forum_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(forumUrl)));
        } catch (Throwable ignored) {
        }
    }
}
