package com.tomclaw.appsend;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Solkin on 11.12.2014.
 */
public class UpdateAppListTask extends PleaseWaitTask {

    private List<AppInfo> appInfoList;

    public UpdateAppListTask(MainActivity activity) {
        super(activity);
    }

    @Override
    public void executeBackground() throws Throwable {
        Context activity = getWeakObject();
        if(activity != null) {
            PackageManager packageManager = activity.getPackageManager();
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> infoList = packageManager.queryIntentActivities(mainIntent, 0);
            appInfoList = new ArrayList<AppInfo>();
            for (ResolveInfo info : infoList) {
                ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, 0);
                    int flags = packageInfo.applicationInfo.flags;
                    Drawable icon = info.loadIcon(packageManager);
                    File file = new File(applicationInfo.publicSourceDir);
                    if(file.exists()) {
                        String label = packageManager.getApplicationLabel(applicationInfo).toString();
                        String version = packageInfo.versionName;
                        long firstInstallTime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? packageInfo.firstInstallTime : 0;
                        long lastUpdateTime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD ? packageInfo.lastUpdateTime : 0;
                        AppInfo appInfo = new AppInfo(icon, label, applicationInfo.packageName, version,
                                file.getPath(), file.length(), firstInstallTime, lastUpdateTime);
                        boolean isUserApp = ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM &&
                                (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != ApplicationInfo.FLAG_UPDATED_SYSTEM_APP);
                        if (isUserApp || PreferenceHelper.isShowSystemApps(activity)) {
                            appInfoList.add(appInfo);
                        }
                    }
                } catch (Throwable ignored) {
                    // Bad package.
                }
            }
            String sortOrder = PreferenceHelper.getSortOrder(activity);
            if(TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_ascending_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return lhs.getLabel().toUpperCase().compareTo(rhs.getLabel().toUpperCase());
                    }
                });
            } else if(TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_descending_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return rhs.getLabel().toUpperCase().compareTo(lhs.getLabel().toUpperCase());
                    }
                });
            } else if(TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_app_size_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return compareLong(rhs.getSize(), lhs.getSize());
                    }
                });
            } else if(TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_install_time_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return compareLong(rhs.getFirstInstallTime(), lhs.getFirstInstallTime());
                    }
                });
            } else if(TextUtils.equals(sortOrder, activity.getString(R.string.sort_order_update_time_value))) {
                Collections.sort(appInfoList, new Comparator<AppInfo>() {
                    @Override
                    public int compare(AppInfo lhs, AppInfo rhs) {
                        return compareLong(rhs.getLastUpdateTime(), lhs.getLastUpdateTime());
                    }
                });
            }
        }
    }

    private int compareLong(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

    @Override
    public void onSuccessMain() {
        MainActivity activity = (MainActivity) getWeakObject();
        if(activity != null) {
            activity.setAppInfoList(appInfoList);
        }
    }

    public static String normalizeVersion(String version) {
        int divider = version.indexOf(' ');
        if(divider == -1) {
            divider = version.indexOf('-');
        } else if(version.indexOf('-') != -1){
            divider = Math.min(divider, version.indexOf('-'));
        }
        if(divider > -1) {
            version = version.substring(0, divider);
        }
        return version;
    }
}
