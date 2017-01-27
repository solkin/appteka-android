package com.tomclaw.appsend;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.tomclaw.appsend.main.controller.ApksController;
import com.tomclaw.appsend.main.controller.DownloadController;
import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.dto.StoreVersion;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.view.PlayView;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.ThemeHelper;
import com.tomclaw.appsend.util.TimeHelper;

import java.io.File;
import java.util.List;

import static com.tomclaw.appsend.util.FileHelper.getExternalDirectory;
import static com.tomclaw.appsend.util.IntentHelper.formatText;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;
import static com.tomclaw.appsend.util.IntentHelper.shareUrl;

/**
 * Created by ivsolkin on 14.01.17.
 */
public class DownloadActivity extends PermisoActivity implements DownloadController.DownloadCallback {

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
    private ViewFlipper buttonsSwitcher;
    private Button buttonOne;
    private Button buttonFirst;
    private Button buttonSecond;
    private ProgressBar progress;

    private StoreInfo info;

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
        buttonsSwitcher = (ViewFlipper) findViewById(R.id.buttons_switcher);
        buttonOne = (Button) findViewById(R.id.button_one);
        buttonFirst = (Button) findViewById(R.id.button_first);
        buttonSecond = (Button) findViewById(R.id.button_second);
        progress = (ProgressBar) findViewById(R.id.progress);
        findViewById(R.id.button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDownload();
            }
        });
        findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadInfo();
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = formatText(getResources(), info.getUrl(),
                        info.getItem().getLabel(), info.getItem().getSize());
                shareUrl(DownloadActivity.this, text);
            }
        });
        findViewById(R.id.play_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGooglePlay(DownloadActivity.this, info.getItem().getPackageName());
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
    public void onBackPressed() {
        finishAttempt(null);
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
    protected void onResume() {
        super.onResume();
        bindButtons();
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
        this.info = info;
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
        bindButtons(item.getPackageName(), item.getVersionCode());
        bindPermissions(item.getPermissions());
        bindVersions(info.getVersions(), item.getVersionCode());
    }

    private void bindButtons() {
        if (info != null) {
            StoreItem item = info.getItem();
            bindButtons(item.getPackageName(), item.getVersionCode());
        }
    }

    private void bindButtons(final String packageName, int versionCode) {
        if (DownloadController.getInstance().isDownloading()) {
            buttonsSwitcher.setDisplayedChild(2);
            return;
        }
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                final Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                boolean isRunnable = launchIntent != null;
                boolean isNewer = versionCode > packageInfo.versionCode;
                if (isRunnable) {
                    buttonsSwitcher.setDisplayedChild(1);
                    buttonFirst.setText(R.string.remove);
                    buttonFirst.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeApp(packageName);
                        }
                    });
                    if (isNewer) {
                        buttonSecond.setText(R.string.update);
                        buttonSecond.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateApp();
                            }
                        });
                    } else {
                        buttonSecond.setText(R.string.open);
                        buttonSecond.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openApp(launchIntent);
                            }
                        });
                    }
                } else {
                    buttonsSwitcher.setDisplayedChild(0);
                    if (isNewer) {
                        buttonOne.setText(R.string.update);
                        buttonOne.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateApp();
                            }
                        });
                    } else {
                        buttonOne.setText(R.string.remove);
                        buttonOne.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                removeApp(packageName);
                            }
                        });
                    }
                }
            }
            return;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        buttonsSwitcher.setDisplayedChild(0);
        buttonOne.setText(R.string.install);
        buttonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsForInstall();
            }
        });
    }

    private void checkPermissionsForInstall() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    // Permission granted!
                    if (!ApksController.getInstance().isStarted()) {
                        installApp();
                    }
                } else {
                    // Permission denied.
                    showError(R.string.write_permission_install);
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = DownloadActivity.this.getString(R.string.app_name);
                String message = DownloadActivity.this.getString(R.string.write_permission_install);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void installApp() {
        File directory = getExternalDirectory();
        File destination = new File(directory, getApkName(info.getItem()));
        if (destination.exists()) {
            destination.delete();
        }
        String filePath = destination.getAbsolutePath();
        DownloadController.getInstance().download(info.getLink(), filePath);
    }

    private void updateApp() {
        checkPermissionsForInstall();
    }

    private void cancelDownload() {
        DownloadController.getInstance().cancelDownload();
    }

    private void removeApp(String packageName) {
        Uri packageUri = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        startActivity(uninstallIntent);
    }

    private void openApp(Intent launchIntent) {
        startActivity(launchIntent);
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
        for (final StoreVersion version : versions) {
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
            versionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAttempt(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(DownloadActivity.this, DownloadActivity.class);
                            intent.putExtra(DownloadActivity.STORE_APP_ID, version.getAppId());
                            intent.putExtra(DownloadActivity.STORE_APP_LABEL, info.getItem().getLabel());
                            startActivity(intent);
                        }
                    });
                }
            });
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

    @Override
    public void onDownloadStarted() {
        buttonsSwitcher.setDisplayedChild(2);
        progress.setIndeterminate(true);
    }

    @Override
    public void onDownloadProgress(long downloadedBytes) {
        progress.setIndeterminate(false);
        StoreItem item = info.getItem();
        if (item.getSize() > 0) {
            progress.setProgress((int) (100 * downloadedBytes / item.getSize()));
        }
    }

    @Override
    public void onDownloaded(String filePath) {
        viewFlipper.setDisplayedChild(0);
        bindButtons();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onDownloadError() {
        showError(R.string.downloading_error);
        viewFlipper.setDisplayedChild(0);
        bindButtons();
    }

    private void showError(@StringRes int message) {
        Snackbar.make(viewFlipper, message, Snackbar.LENGTH_SHORT);
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

    private static String getApkPrefix(StoreItem item) {
        return FileHelper.escapeFileSymbols(item.getLabel() + "-" + item.getVersion());
    }

    private static String getApkSuffix() {
        return ".apk";
    }

    private static String getApkName(StoreItem item) {
        return getApkPrefix(item) + getApkSuffix();
    }

    private void finishAttempt(final Runnable runnable) {
        if (DownloadController.getInstance().isDownloading()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.cancel_download_title))
                    .setMessage(getString(R.string.cancel_download_text))
                    .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelDownload();
                            finish();
                            if (runnable != null) {
                                runnable.run();
                            }
                        }
                    })
                    .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            finish();
            if (runnable != null) {
                runnable.run();
            }
        }
    }
}
