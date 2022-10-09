package com.tomclaw.appsend.main.local;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;
import static com.tomclaw.appsend.main.download.DownloadActivity.createAppActivityIntent;
import static com.tomclaw.appsend.screen.details.DetailsActivityKt.createDetailsActivityIntent;
import static com.tomclaw.appsend.util.IntentHelper.bluetoothApk;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;
import static com.tomclaw.appsend.util.IntentHelper.shareApk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.widget.ListAdapter;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.greysonparrelli.permiso.Permiso;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.main.permissions.PermissionsActivity_;
import com.tomclaw.appsend.main.permissions.PermissionsList;
import com.tomclaw.appsend.main.upload.UploadActivity;
import com.tomclaw.appsend.util.IntentHelper;

import org.androidannotations.annotations.EFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@EFragment(R.layout.local_apps_fragment)
public class HomeDistroFragment extends DistroFragment {

    @Override
    public void loadAttempt() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    invalidate();
                    loadFiles();
                } else {
                    errorText.setText(R.string.write_permission_install);
                    showError();
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = getContext().getString(R.string.app_name);
                String message = getContext().getString(R.string.write_permission_install);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onClick(final ApkItem item) {
        ListAdapter menuAdapter = new MenuAdapter(getContext(), R.array.apk_actions_titles, R.array.apk_actions_icons);
        new AlertDialog.Builder(getContext())
                .setAdapter(menuAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            setRefreshOnResume();
                            installApp(item);
                            trackEvent("click-install-apk");
                            break;
                        }
                        case 1: {
                            shareApk(getContext(), new File(item.getPath()));
                            trackEvent("click-share-apk");
                            break;
                        }
                        case 2: {
                            Intent intent = new Intent(getContext(), UploadActivity.class);
                            intent.putExtra(UploadActivity.UPLOAD_ITEM, item);
                            startActivity(intent);
                            trackEvent("click-upload-apk");
                            break;
                        }
                        case 3: {
                            bluetoothApk(getContext(), new File(item.getPath()));
                            trackEvent("click-bluetooth-share");
                            break;
                        }
                        case 4: {
                            final String packageName = item.getPackageName();
                            openGooglePlay(getContext(), packageName);
                            trackEvent("click-search-google-play");
                            break;
                        }
                        case 5: {
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
                        case 6: {
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
                        case 7: {
                            File file = new File(item.getPath());
                            if (file.delete()) {
                                reloadFiles();
                            }
                            trackEvent("click-delete-app");
                            break;
                        }
                    }
                }).show();
    }

    private void installApp(ApkItem item) {
        IntentHelper.openFile(getContext(), item.getPath(), "application/vnd.android.package-archive");
    }
}
