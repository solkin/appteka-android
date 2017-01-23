package com.tomclaw.appsend;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.main.controller.DownloadController;
import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.dto.StoreVersion;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.view.PlayView;
import com.tomclaw.appsend.util.ThemeHelper;
import com.tomclaw.appsend.util.TimeHelper;

import java.util.List;

/**
 * Created by ivsolkin on 14.01.17.
 */
public class DownloadActivity extends AppCompatActivity implements DownloadController.DownloadCallback {

    public static final String STORE_APP_ID = "app_id";
    public static final String STORE_APP_LABEL = "app_label";

    private static final int MAX_PERMISSIONS_COUNT = 3;

    private TimeHelper timeHelper;

    private String appId;
    private String appLabel;

    private ViewFlipper viewFlipper;
    private ImageView iconView;
    private TextView labelView;
    private TextView packageView;
    private PlayView downloadsView;
    private PlayView sizeView;
    private PlayView minAndroidView;
    private RelativeLayout permissionsBlock;
    private ViewGroup permissionsContainer;
    private TextView versionView;
    private TextView uploadedTimeView;
    private TextView checksumView;
    private View shadowView;
    private View readMoreButton;
    private View otherVersionsTitle;
    private ViewGroup versionsContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.download_activity);

        timeHelper = new TimeHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        boolean isCreateInstance = savedInstanceState == null;
        if (isCreateInstance) {
            appId = getIntent().getStringExtra(STORE_APP_ID);
            appLabel = getIntent().getStringExtra(STORE_APP_LABEL);
        } else {
            appId = savedInstanceState.getString(STORE_APP_ID);
            appLabel = savedInstanceState.getString(STORE_APP_LABEL);
        }

        setTitle(appLabel);

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        iconView = (ImageView) findViewById(R.id.app_icon);
        labelView = (TextView) findViewById(R.id.app_label);
        packageView = (TextView) findViewById(R.id.app_package);
        downloadsView = (PlayView) findViewById(R.id.app_downloads);
        sizeView = (PlayView) findViewById(R.id.app_size);
        minAndroidView = (PlayView) findViewById(R.id.min_android);
        permissionsBlock = (RelativeLayout) findViewById(R.id.permissions_block);
        permissionsContainer = (ViewGroup) findViewById(R.id.permissions_container);
        versionView = (TextView) findViewById(R.id.app_version);
        uploadedTimeView = (TextView) findViewById(R.id.uploaded_time);
        checksumView = (TextView) findViewById(R.id.app_checksum);
        shadowView = findViewById(R.id.read_more_shadow);
        readMoreButton = findViewById(R.id.read_more_button);
        otherVersionsTitle = findViewById(R.id.other_versions_title);
        versionsContainer = (ViewGroup) findViewById(R.id.app_versions);
        findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadInfo();
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        findViewById(R.id.browser_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        if (isCreateInstance) {
            loadInfo();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        DownloadController.getInstance().onAttach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownloadController.getInstance().onDetach(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STORE_APP_ID, appId);
        outState.putString(STORE_APP_LABEL, appLabel);
    }

    private void loadInfo() {
        DownloadController.getInstance().loadInfo(appId);
    }

    private void reloadInfo() {
        if (!DownloadController.getInstance().isStarted()) {
            loadInfo();
        }
    }

    private void bindStoreItem(StoreInfo info) {
        StoreItem item = info.getItem();
        Glide.with(this)
                .load(item.getIcon())
                .into(iconView);
        String sizeText;
        int sizeFactor;
        long bytes = item.getSize();
        if (bytes < 1024 * 1024) {
            sizeText = String.format("%.1f", bytes / 1024.0f);
            sizeFactor = R.string.kilobytes;
        } else if (bytes < 10 * 1024 * 1024) {
            sizeText = String.format("%.1f", bytes / 1024.0f / 1024.0f);
            sizeFactor = R.string.megabytes;
        } else {
            sizeText = String.format("%d", bytes / 1024 / 1024);
            sizeFactor = R.string.megabytes;
        }
        labelView.setText(item.getLabel());
        packageView.setText(item.getPackageName());
        downloadsView.setCount(String.valueOf(item.getDownloads()));
        sizeView.setCount(sizeText);
        sizeView.setDescription(getString(sizeFactor));
        minAndroidView.setCount(item.getAndroidVersion());
        versionView.setText(getString(R.string.app_version_format, item.getVersion(), item.getVersionCode()));
        uploadedTimeView.setText(timeHelper.getFormattedDate(item.getTime()));
        checksumView.setText(item.getSha1());
        bindButtons(item.getPackageName());
        bindPermissions(item.getPermissions());
        bindVersions(info.getVersions(), item.getVersionCode());
    }

    private void bindButtons(String packageName) {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            
        } catch (PackageManager.NameNotFoundException ignored) {

        }
    }

    private void bindPermissions(List<String> permissions) {
        final boolean hasPermissions = !permissions.isEmpty();
        int count = Math.min(MAX_PERMISSIONS_COUNT, permissions.size());
        permissionsContainer.removeAllViews();
        for (int c = 0; c < count; c++) {
            String permission = permissions.get(c);
            View permissionView = getLayoutInflater().inflate(R.layout.permission_view, permissionsContainer, false);
            TextView permissionDescription = (TextView) permissionView.findViewById(R.id.permission_description);
            TextView permissionName = (TextView) permissionView.findViewById(R.id.permission_name);
            String description = getPermissionDescription(this, permission);
            permissionDescription.setText(description);
            permissionName.setText(permission);
            permissionsContainer.addView(permissionView);
        }
        permissionsBlock.setVisibility(hasPermissions ? View.VISIBLE : View.GONE);
        boolean isOverflow = permissions.size() > MAX_PERMISSIONS_COUNT;
        readMoreButton.setVisibility(hasPermissions && isOverflow ? View.VISIBLE : View.GONE);
        shadowView.setVisibility(readMoreButton.getVisibility());
    }

    private void bindVersions(List<StoreVersion> versions, int versionCode) {
        versionsContainer.removeAllViews();
        boolean isVersionsAdded = false;
        for (StoreVersion version : versions) {
            if (version.getVerCode() == versionCode) {
                continue;
            }
            View versionView = getLayoutInflater().inflate(R.layout.version_view, versionsContainer, false);
            TextView versionNameView = (TextView) versionView.findViewById(R.id.app_version_name);
            TextView versionCodeView = (TextView) versionView.findViewById(R.id.app_version_code);
            TextView versionDownloads = (TextView) versionView.findViewById(R.id.app_version_downloads);
            View newerBadge = versionView.findViewById(R.id.app_newer_badge);
            versionNameView.setText(version.getVerName());
            versionCodeView.setText('(' + String.valueOf(version.getVerCode()) + ')');
            versionDownloads.setText(String.valueOf(version.getDownloads()));
            boolean isNewer = version.getVerCode() > versionCode;
            newerBadge.setVisibility(isNewer ? View.VISIBLE : View.GONE);
            versionsContainer.addView(versionView);
            isVersionsAdded = true;
        }
        versionsContainer.setVisibility(isVersionsAdded ? View.VISIBLE : View.GONE);
        otherVersionsTitle.setVisibility(versionsContainer.getVisibility());
    }

    @Override
    public void onInfoLoaded(StoreInfo storeInfo) {
        bindStoreItem(storeInfo);
        viewFlipper.setDisplayedChild(1);
    }

    @Override
    public void onInfoError() {
        viewFlipper.setDisplayedChild(2);
    }

    @Override
    public void onInfoProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    @Nullable
    private static String getPermissionDescription(@NonNull Context context, @NonNull String permission) {
        String description;
        try {
            PackageManager packageManager = context.getPackageManager();
            PermissionInfo permissionInfo = packageManager
                    .getPermissionInfo(permission, PackageManager.GET_META_DATA);
            description = permissionInfo.loadLabel(packageManager).toString();
        } catch (Throwable ignored) {
            description = context.getString(R.string.unknown_permission_description);
        }
        return description;
    }
}
