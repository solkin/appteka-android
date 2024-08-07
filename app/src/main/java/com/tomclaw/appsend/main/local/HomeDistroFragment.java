package com.tomclaw.appsend.main.local;

import static com.tomclaw.appsend.screen.details.DetailsActivityKt.createDetailsActivityIntent;
import static com.tomclaw.appsend.screen.permissions.PermissionsActivityKt.createPermissionsActivityIntent;
import static com.tomclaw.appsend.screen.upload.UploadActivityKt.createUploadActivityIntent;
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
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.upload.UploadApk;
import com.tomclaw.appsend.upload.UploadPackage;
import com.tomclaw.appsend.util.IntentHelper;

import org.androidannotations.annotations.EFragment;

import java.io.File;
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
                            injector.analytics.trackEvent("click-install-apk");
                            break;
                        }
                        case 1: {
                            shareApk(getContext(), new File(item.getPath()));
                            injector.analytics.trackEvent("click-share-apk");
                            break;
                        }
                        case 2: {
                            UploadPackage pkg = new UploadPackage(item.getPath(), null, item.getPackageName());
                            UploadApk apk = new UploadApk(item.getPath(), item.getVersion(), item.getSize(), item.getPackageInfo());
                            Intent intent = createUploadActivityIntent(getContext(), pkg, apk, null);
                            startActivity(intent);
                            injector.analytics.trackEvent("click-upload-apk");
                            break;
                        }
                        case 3: {
                            bluetoothApk(getContext(), new File(item.getPath()));
                            injector.analytics.trackEvent("click-bluetooth-share");
                            break;
                        }
                        case 4: {
                            final String packageName = item.getPackageName();
                            openGooglePlay(getContext(), packageName);
                            injector.analytics.trackEvent("click-search-google-play");
                            break;
                        }
                        case 5: {
                            String packageName = item.getPackageName();
                            String label = item.getLabel();
                            Intent intent = createDetailsActivityIntent(
                                    getContext(),
                                    null,
                                    packageName,
                                    label,
                                    false,
                                    true
                            );
                            startActivity(intent);
                            injector.analytics.trackEvent("click-search-appteka");
                            break;
                        }
                        case 6: {
                            try {
                                PackageInfo packageInfo = item.getPackageInfo();
                                List<String> permissions = Arrays.asList(packageInfo.requestedPermissions);
                                Intent intent = createPermissionsActivityIntent(getContext(), permissions);
                                startActivity(intent);
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
                            injector.analytics.trackEvent("click-delete-app");
                            break;
                        }
                    }
                }).show();
    }

    private void installApp(ApkItem item) {
        IntentHelper.openFile(getContext(), item.getPath(), "application/vnd.android.package-archive");
    }
}
