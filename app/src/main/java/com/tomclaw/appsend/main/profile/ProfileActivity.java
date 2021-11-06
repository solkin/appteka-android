package com.tomclaw.appsend.main.profile;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;
import static com.tomclaw.appsend.util.IntentHelper.formatText;
import static com.tomclaw.appsend.util.IntentHelper.shareUrl;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.home.HomeActivity;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by solkin on 16/03/2018.
 */
@SuppressLint("Registered")
@EActivity(R.layout.profile_activity)
public class ProfileActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @FragmentById
    ProfileFragment profileFragment;

    @Extra
    Long userId;

    @InstanceState
    Boolean isShowHomeOnFinish;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
        boolean isCreateInstance = savedInstanceState == null;
        if (isCreateInstance) {
            Uri data = getIntent().getData();
            if (data != null && data.getHost() != null) {
                if (data.getHost().equals("appteka.store")) {
                    List<String> path = data.getPathSegments();
                    if (path.size() == 2) {
                        userId = Long.parseLong(path.get(1));
                        isShowHomeOnFinish = true;
                    }
                } else if (data.getHost().equals("appsend.store")) {
                    userId = Long.parseLong(data.getQueryParameter("id"));
                    isShowHomeOnFinish = true;
                }
            }
        }
        if (isCreateInstance) {
            trackEvent("open-profile-screen");
        }
    }

    @AfterViews
    void init() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        profileFragment.setUserId(userId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (profileFragment.profile != null) {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.share:
                String text = getResources().getString(R.string.user_url, profileFragment.memberName.getText(), profileFragment.profile.getUserId(), profileFragment.profile.getUrl());
                shareUrl(ProfileActivity.this, text);
                trackEvent("share-user-url");
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isShowHomeOnFinish != null && isShowHomeOnFinish) {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class)
                    .setAction(HomeActivity.ACTION_STORE)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
