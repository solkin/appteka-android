package com.tomclaw.appsend.main.local;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.ListAdapter;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.main.permissions.PermissionsActivity_;
import com.tomclaw.appsend.main.permissions.PermissionsList;
import com.tomclaw.appsend.main.upload.UploadActivity;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.IntentHelper;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.pm.PackageManager.GET_PERMISSIONS;
import static com.tomclaw.appsend.util.IntentHelper.bluetoothApk;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;
import static com.tomclaw.appsend.util.IntentHelper.shareApk;

@EFragment(R.layout.local_apps_fragment)
public class DistroFragment extends CommonItemFragment<ApkItem> {

    private static final CharSequence APK_EXTENSION = "apk";

    @InstanceState
    ArrayList<ApkItem> files;

    private boolean isRefreshOnResume = false;

    @Override
    public void onResume() {
        super.onResume();
        if (isRefreshOnResume) {
            isRefreshOnResume = false;
            reloadFiles();
        }
    }

    @Override
    protected List<ApkItem> getFiles() {
        return files;
    }

    @Override
    protected void setFiles(List<ApkItem> files) {
        if (files != null) {
            this.files = new ArrayList<>(files);
        } else {
            this.files = null;
        }
    }

    @Override
    protected FileViewHolderCreator<ApkItem> getViewHolderCreator() {
        return new ApkItemViewHolderCreator(getContext());
    }

    @Override
    List<ApkItem> loadItemsSync() {
        PackageManager packageManager = getContext().getPackageManager();
        ArrayList<ApkItem> itemList = new ArrayList<>();
        walkDir(packageManager, itemList, Environment.getExternalStorageDirectory());
        return itemList;
    }

    private void walkDir(PackageManager packageManager, List<ApkItem> itemList, File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (File file : listFile) {
                if (file.isDirectory()) {
                    walkDir(packageManager, itemList, file);
                } else {
                    String extension = FileHelper.getFileExtensionFromPath(file.getName());
                    if (TextUtils.equals(extension, APK_EXTENSION)) {
                        processApk(packageManager, itemList, file);
                    }
                }
            }
        }
    }

    private void processApk(PackageManager packageManager, List<ApkItem> itemList, File file) {
        if (file.exists()) {
            try {
                PackageInfo packageInfo = packageManager.getPackageArchiveInfo(
                        file.getAbsolutePath(), GET_PERMISSIONS);
                if (packageInfo != null) {
                    ApplicationInfo info = packageInfo.applicationInfo;
                    info.sourceDir = file.getAbsolutePath();
                    info.publicSourceDir = file.getAbsolutePath();
                    String label = packageManager.getApplicationLabel(info).toString();
                    String version = packageInfo.versionName;

                    String installedVersion = null;
                    try {
                        PackageInfo instPackageInfo = packageManager.getPackageInfo(info.packageName, 0);
                        if (instPackageInfo != null) {
                            installedVersion = instPackageInfo.versionName;
                        }
                    } catch (Throwable ignored) {
                        // No package, maybe?
                    }

                    ApkItem item = new ApkItem(label, info.packageName, version, file.getPath(),
                            file.length(), installedVersion, file.lastModified(), packageInfo);
                    itemList.add(item);
                }
            } catch (Throwable ignored) {
                // Bad package.
            }
        }
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

    private void setRefreshOnResume() {
        isRefreshOnResume = true;
    }

    private void installApp(ApkItem item) {
        IntentHelper.openFile(getContext(), item.getPath(), "application/vnd.android.package-archive");
    }
}
