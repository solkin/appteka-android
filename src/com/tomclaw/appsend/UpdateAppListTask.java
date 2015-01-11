package com.tomclaw.appsend;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.util.ArrayList;
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
                        String version = normalizeVersion(packageInfo.versionName);
                        AppInfo appInfo = new AppInfo(icon, label, applicationInfo.packageName, version,
                                file.getPath(), file.length(), packageInfo.lastUpdateTime);
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
        }
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
