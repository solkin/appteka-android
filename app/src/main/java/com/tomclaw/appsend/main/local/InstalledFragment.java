package com.tomclaw.appsend.main.local;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.ListAdapter;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.main.permissions.PermissionsActivity_;
import com.tomclaw.appsend.main.permissions.PermissionsList;
import com.tomclaw.appsend.main.task.ExportApkTask;
import com.tomclaw.appsend.main.upload.UploadActivity;
import com.tomclaw.appsend.util.PreferenceHelper;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.content.pm.PackageManager.GET_PERMISSIONS;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;

@EFragment(R.layout.local_apps_fragment)
public class InstalledFragment extends CommonItemFragment<AppItem> {

    @InstanceState
    ArrayList<AppItem> files;

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
    protected List<AppItem> getFiles() {
        return files;
    }

    @Override
    protected void setFiles(List<AppItem> files) {
        if (files != null) {
            this.files = new ArrayList<>(files);
        } else {
            this.files = null;
        }
    }

    @Override
    protected FileViewHolderCreator<AppItem> getViewHolderCreator() {
        return new AppItemViewHolderCreator(getContext());
    }

    @Override
    List<AppItem> loadItemsSync() {
        final Locale locale = Locale.getDefault();
        final Context context = getContext();
        PackageManager packageManager = context.getPackageManager();
        ArrayList<AppItem> appItemList = new ArrayList<>();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info : packages) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(
                        info.packageName, GET_PERMISSIONS);
                File file = new File(info.publicSourceDir);
                if (file.exists()) {
                    String label = packageManager.getApplicationLabel(info).toString();
                    String version = packageInfo.versionName;
                    long firstInstallTime = packageInfo.firstInstallTime;
                    long lastUpdateTime = packageInfo.lastUpdateTime;
                    Intent launchIntent = packageManager.getLaunchIntentForPackage(info.packageName);
                    AppItem appItem = new AppItem(label, info.packageName, version, file.getPath(),
                            file.length(), firstInstallTime, lastUpdateTime, packageInfo);
                    boolean isUserApp = ((info.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM &&
                            (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != ApplicationInfo.FLAG_UPDATED_SYSTEM_APP);
                    if (isUserApp || PreferenceHelper.isShowSystemApps(context)) {
                        if (launchIntent != null || !PreferenceHelper.isRunnableOnly(context)) {
                            appItemList.add(appItem);
                        }
                    }
                }
            } catch (Throwable ignored) {
                // Bad package.
            }
        }
        String sortOrder = PreferenceHelper.getSortOrder(context);
        if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_ascending_value))) {
            Collections.sort(appItemList, new Comparator<AppItem>() {
                @Override
                public int compare(AppItem lhs, AppItem rhs) {
                    return lhs.getLabel().toUpperCase(locale)
                            .compareTo(rhs.getLabel().toUpperCase(locale));
                }
            });
        } else if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_descending_value))) {
            Collections.sort(appItemList, new Comparator<AppItem>() {
                @Override
                public int compare(AppItem lhs, AppItem rhs) {
                    return rhs.getLabel().toUpperCase(locale)
                            .compareTo(lhs.getLabel().toUpperCase(locale));
                }
            });
        } else if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_app_size_value))) {
            Collections.sort(appItemList, new Comparator<AppItem>() {
                @Override
                public int compare(AppItem lhs, AppItem rhs) {
                    return compareLong(rhs.getSize(), lhs.getSize());
                }
            });
        } else if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_install_time_value))) {
            Collections.sort(appItemList, new Comparator<AppItem>() {
                @Override
                public int compare(AppItem lhs, AppItem rhs) {
                    return compareLong(rhs.getFirstInstallTime(), lhs.getFirstInstallTime());
                }
            });
        } else if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_update_time_value))) {
            Collections.sort(appItemList, new Comparator<AppItem>() {
                @Override
                public int compare(AppItem lhs, AppItem rhs) {
                    return compareLong(rhs.getLastUpdateTime(), lhs.getLastUpdateTime());
                }
            });
        }
        return appItemList;
    }

    private int compareLong(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    @Override
    public void onClick(final AppItem item) {
        ListAdapter menuAdapter = new MenuAdapter(getContext(), R.array.app_actions_titles, R.array.app_actions_icons);
        new AlertDialog.Builder(getContext())
                .setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                FlurryAgent.logEvent("App menu: run");
                                PackageManager packageManager = getContext().getPackageManager();
                                Intent launchIntent = packageManager.getLaunchIntentForPackage(item.getPackageName());
                                if (launchIntent == null) {
                                    Snackbar.make(recycler, R.string.non_launchable_package, Snackbar.LENGTH_LONG).show();
                                } else {
                                    startActivity(launchIntent);
                                }
                                break;
                            }
                            case 1: {
                                FlurryAgent.logEvent("App menu: share");
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_SHARE));
                                break;
                            }
                            case 2: {
                                FlurryAgent.logEvent("App menu: extract");
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_EXTRACT));
                                break;
                            }
                            case 3: {
                                FlurryAgent.logEvent("App menu: upload");
                                Intent intent = new Intent(getContext(), UploadActivity.class);
                                intent.putExtra(UploadActivity.UPLOAD_ITEM, item);
                                startActivity(intent);
                                break;
                            }
                            case 4: {
                                FlurryAgent.logEvent("App menu: bluetooth");
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_BLUETOOTH));
                                break;
                            }
                            case 5: {
                                FlurryAgent.logEvent("App menu: Google Play");
                                String packageName = item.getPackageName();
                                openGooglePlay(getContext(), packageName);
                                break;
                            }
                            case 6: {
                                FlurryAgent.logEvent("App menu: AppSend Store");
                                Intent intent = new Intent(getContext(), DownloadActivity.class);
                                intent.putExtra(DownloadActivity.STORE_APP_PACKAGE, item.getPackageName());
                                intent.putExtra(DownloadActivity.STORE_APP_LABEL, item.getLabel());
                                startActivity(intent);
                                break;
                            }
                            case 7: {
                                FlurryAgent.logEvent("App menu: permissions");
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
                                FlurryAgent.logEvent("App menu: details");
                                setRefreshOnResume();
                                final Intent intent = new Intent()
                                        .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .addCategory(Intent.CATEGORY_DEFAULT)
                                        .setData(Uri.parse("package:" + item.getPackageName()))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                break;
                            }
                            case 9: {
                                FlurryAgent.logEvent("App menu: remove");
                                setRefreshOnResume();
                                Uri packageUri = Uri.parse("package:" + item.getPackageName());
                                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                                startActivity(uninstallIntent);
                                break;
                            }
                        }
                    }
                }).show();
    }

    private void setRefreshOnResume() {
        isRefreshOnResume = true;
    }
}
