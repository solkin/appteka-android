package com.tomclaw.appsend.main.local;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;
import static com.tomclaw.appsend.main.download.DownloadActivity.createAppActivityIntent;
import static com.tomclaw.appsend.screen.details.DetailsActivityKt.createDetailsActivityIntent;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.ListAdapter;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.greysonparrelli.permiso.Permiso;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.main.permissions.PermissionsActivity_;
import com.tomclaw.appsend.main.permissions.PermissionsList;
import com.tomclaw.appsend.main.task.ExportApkTask;
import com.tomclaw.appsend.main.upload.UploadActivity;

import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EFragment(R.layout.local_apps_fragment)
public class HomeInstalledFragment extends InstalledFragment {

    private void checkExtractPermissions(final AppItem item) {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    showActionDialog(item);
                } else {
                    Snackbar.make(recycler, R.string.permission_denied_message, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = getResources().getString(R.string.app_name);
                String message = getResources().getString(R.string.write_permission_extract);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onClick(final AppItem item) {
        checkExtractPermissions(item);
    }

    private void showActionDialog(final AppItem item) {
        ListAdapter menuAdapter = new MenuAdapter(getContext(), R.array.app_actions_titles, R.array.app_actions_icons);
        new AlertDialog.Builder(getContext())
                .setAdapter(menuAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            PackageManager packageManager = getContext().getPackageManager();
                            Intent launchIntent = packageManager.getLaunchIntentForPackage(item.getPackageName());
                            if (launchIntent == null) {
                                Snackbar.make(recycler, R.string.non_launchable_package, Snackbar.LENGTH_LONG).show();
                            } else {
                                startActivity(launchIntent);
                            }
                            trackEvent("click-run-app");
                            break;
                        }
                        case 1: {
                            TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_SHARE));
                            trackEvent("click-share-apk");
                            break;
                        }
                        case 2: {
                            TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_EXTRACT));
                            trackEvent("click-extract-apk");
                            break;
                        }
                        case 3: {
                            Intent intent = new Intent(getContext(), UploadActivity.class);
                            intent.putExtra(UploadActivity.UPLOAD_ITEM, item);
                            startActivity(intent);
                            trackEvent("click-upload-apk");
                            break;
                        }
                        case 4: {
                            TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_BLUETOOTH));
                            trackEvent("click-bluetooth-share");
                            break;
                        }
                        case 5: {
                            String packageName = item.getPackageName();
                            openGooglePlay(getContext(), packageName);
                            trackEvent("click-search-google-play");
                            break;
                        }
                        case 6: {
                            String packageName = item.getPackageName();
                            String label = item.getLabel();
                            Intent intent = Config.NEW_DETAILS_SCREEN ?
                                    createDetailsActivityIntent(
                                            getContext(),
                                            null,
                                            packageName,
                                            label,
                                            false,
                                            true
                                    )
                                    :
                                    createAppActivityIntent(
                                            getContext(),
                                            packageName,
                                            label,
                                            true
                                    );
                            startActivity(intent);
                            trackEvent("click-search-appteka");
                            break;
                        }
                        case 7: {
                            try {
                                PackageInfo packageInfo = item.getPackageInfo();
                                List<String> permissions = Arrays.asList(packageInfo.requestedPermissions);
                                PermissionsActivity_.intent(getContext())
                                        .permissions(new PermissionsList(new ArrayList<>(permissions)))
                                        .start();
                            } catch (Throwable ex) {
                                Snackbar.make(recycler, R.string.unable_to_get_permissions, Snackbar.LENGTH_LONG).show();
                            }
                            break;
                        }
                        case 8: {
                            setRefreshOnResume();
                            final Intent intent = new Intent()
                                    .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .addCategory(Intent.CATEGORY_DEFAULT)
                                    .setData(Uri.parse("package:" + item.getPackageName()))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            trackEvent("click-app-details");
                            break;
                        }
                        case 9: {
                            setRefreshOnResume();
                            Uri packageUri = Uri.parse("package:" + item.getPackageName());
                            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                            startActivity(uninstallIntent);
                            trackEvent("click-uninstall-app");
                            break;
                        }
                    }
                }).show();
    }
}
