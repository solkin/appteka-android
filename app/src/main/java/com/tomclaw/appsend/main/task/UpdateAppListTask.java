package com.tomclaw.appsend.main.task;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.tomclaw.appsend.AppInfo;
import com.tomclaw.appsend.MainActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.PleaseWaitTask;
import com.tomclaw.appsend.util.PreferenceHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by Solkin on 11.12.2014.
 */
public class UpdateAppListTask extends PleaseWaitTask {

    private List<AppInfo> appInfoList;

    public UpdateAppListTask(Context activity) {
        super(activity);
    }

    @Override
    public void executeBackground() throws Throwable {
        Context activity = getWeakObject();
        if (activity != null) {
            PackageManager packageManager = activity.getPackageManager();
            appInfoList = new ArrayList<>();
            List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo info : packages) {
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(info.packageName, 0);
                    int flags = packageInfo.applicationInfo.flags;
                    File file = new File(info.publicSourceDir);
                    if (file.exists()) {
                        String label = packageManager.getApplicationLabel(info).toString();
                        String version = packageInfo.versionName;
                        long firstInstallTime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? packageInfo.firstInstallTime : 0;
                        long lastUpdateTime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? packageInfo.lastUpdateTime : 0;
                        Intent launchIntent = packageManager.getLaunchIntentForPackage(info.packageName);
                        AppInfo appInfo = new AppInfo(label, info.packageName,
                                version, null, file.getPath(), file.length(), firstInstallTime,
                                lastUpdateTime, launchIntent, AppInfo.FLAG_INSTALLED_APP);
                        boolean isUserApp = ((info.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM &&
                                (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != ApplicationInfo.FLAG_UPDATED_SYSTEM_APP);
                        if (isUserApp || PreferenceHelper.isShowSystemApps(activity)) {
                            if (launchIntent != null || !PreferenceHelper.isRunnableOnly(activity)) {
                                appInfoList.add(appInfo);
                            }
                        }
                    }
                } catch (Throwable ignored) {
                    // Bad package.
                }
            }
            String sortOrder = PreferenceHelper.getSortOrder(activity);
            if (TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_ascending_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return lhs.getLabel().toUpperCase().compareTo(rhs.getLabel().toUpperCase());
                    }
                });
            } else if (TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_descending_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return rhs.getLabel().toUpperCase().compareTo(lhs.getLabel().toUpperCase());
                    }
                });
            } else if (TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_app_size_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return compareLong(rhs.getSize(), lhs.getSize());
                    }
                });
            } else if (TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_install_time_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return compareLong(rhs.getFirstInstallTime(), lhs.getFirstInstallTime());
                    }
                });
            } else if (TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_update_time_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return compareLong(rhs.getLastUpdateTime(), lhs.getLastUpdateTime());
                    }
                });
            }
            int count = Math.min(appInfoList.size(), 8);
            Random random = new Random(System.currentTimeMillis());
            int position = random.nextInt(count);
            // Oh, this is ugly, very ugly decision... But I really want to sleep.
            // TODO: Refactor me, please!
            AppInfo donateItem = new AppInfo(null, null, null, null, null, -1, -1, -1, null, AppInfo.FLAG_DONATE_ITEM);
            appInfoList.add(position, donateItem);
        }
    }

    private int compareLong(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    @Override
    public void onSuccessMain() {
        MainActivity activity = (MainActivity) getWeakObject();
        if (activity != null) {
//            activity.setItemsList(appInfoList);
        }
    }

    public static String normalizeVersion(String version) {
        int divider = version.indexOf(' ');
        if (divider == -1) {
            divider = version.indexOf('-');
        } else if (version.indexOf('-') != -1) {
            divider = Math.min(divider, version.indexOf('-'));
        }
        if (divider > -1) {
            version = version.substring(0, divider);
        }
        return version;
    }
}
