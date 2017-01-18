package com.tomclaw.appsend;

import android.content.Context;
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
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.main.controller.DownloadController;
import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.view.MaxHeightFrameLayout;
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
    private MaxHeightFrameLayout permissionsBlock;
    private ViewGroup permissionsContainer;
    private TextView versionView;
    private TextView uploadedTimeView;
    private TextView checksumView;
    private View shadowView;
    private View readMoreButton;

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
        permissionsBlock = (MaxHeightFrameLayout) findViewById(R.id.permissions_block);
        permissionsContainer = (ViewGroup) findViewById(R.id.permissions_container);
        versionView = (TextView) findViewById(R.id.app_version);
        uploadedTimeView = (TextView) findViewById(R.id.uploaded_time);
        checksumView = (TextView) findViewById(R.id.app_checksum);
        shadowView = findViewById(R.id.read_more_shadow);
        readMoreButton = findViewById(R.id.read_more_button);
        findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadInfo();
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

    private void bindStoreItem(StoreItem item) {
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
        bindPermissions(item.getPermissions());
    }

    private void bindPermissions(List<String> permissions) {
        final boolean hasPermissions = !permissions.isEmpty();
        for (String permission : permissions) {
            View permissionView = getLayoutInflater().inflate(R.layout.permission_view, permissionsContainer, false);
            TextView permissionDescription = (TextView) permissionView.findViewById(R.id.permission_description);
            TextView permissionName = (TextView) permissionView.findViewById(R.id.permission_name);
            String description = getPermissionDescription(this, permission);
            permissionDescription.setText(description);
            permissionName.setText(permission);
            permissionsContainer.addView(permissionView);
        }
        permissionsBlock.setVisibility(hasPermissions ? View.VISIBLE : View.GONE);
        readMoreButton.setVisibility(hasPermissions ? View.VISIBLE : View.GONE);
        shadowView.setVisibility(readMoreButton.getVisibility());
        if (hasPermissions) {
            permissionsBlock.post(new Runnable() {
                @Override
                public void run() {
                    readMoreButton.setVisibility(permissionsBlock.isOverflow() ? View.VISIBLE : View.GONE);
                    shadowView.setVisibility(readMoreButton.getVisibility());
                }
            });
        }
    }

    @Override
    public void onInfoLoaded(StoreInfo storeInfo) {
        bindStoreItem(storeInfo.getInfo());
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
