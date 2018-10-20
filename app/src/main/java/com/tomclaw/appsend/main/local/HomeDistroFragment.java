package com.tomclaw.appsend.main.local;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import android.widget.ListAdapter;

import com.flurry.android.FlurryAgent;
import com.greysonparrelli.permiso.Permiso;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.download.DownloadActivity;
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

import static com.tomclaw.appsend.util.IntentHelper.bluetoothApk;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;
import static com.tomclaw.appsend.util.IntentHelper.shareApk;

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
                .setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                FlurryAgent.logEvent("Install menu: install");
                                setRefreshOnResume();
                                installApp(item);
                                break;
                            }
                            case 1: {
                                FlurryAgent.logEvent("Install menu: share");
                                shareApk(getContext(), new File(item.getPath()));
                                break;
                            }
                            case 2: {
                                FlurryAgent.logEvent("Install menu: upload");
                                Intent intent = new Intent(getContext(), UploadActivity.class);
                                intent.putExtra(UploadActivity.UPLOAD_ITEM, item);
                                startActivity(intent);
                                break;
                            }
                            case 3: {
                                FlurryAgent.logEvent("Install menu: bluetooth");
                                bluetoothApk(getContext(), item);
                                break;
                            }
                            case 4: {
                                FlurryAgent.logEvent("Install menu: Google Play");
                                final String packageName = item.getPackageName();
                                openGooglePlay(getContext(), packageName);
                                break;
                            }
                            case 5: {
                                FlurryAgent.logEvent("Install menu: AppSend Store");
                                Intent intent = new Intent(getContext(), DownloadActivity.class);
                                intent.putExtra(DownloadActivity.STORE_APP_PACKAGE, item.getPackageName());
                                intent.putExtra(DownloadActivity.STORE_APP_LABEL, item.getLabel());
                                startActivity(intent);
                                break;
                            }
                            case 6: {
                                FlurryAgent.logEvent("Install menu: permissions");
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
                                FlurryAgent.logEvent("Install menu: remove");
                                File file = new File(item.getPath());
                                if (file.delete()) {
                                    reloadFiles();
                                }
                                break;
                            }
                        }
                    }
                }).show();
    }

    private void installApp(ApkItem item) {
        IntentHelper.openFile(getContext(), item.getPath(), "application/vnd.android.package-archive");
    }
}
