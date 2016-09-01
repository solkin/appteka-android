package com.tomclaw.appsend;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by Solkin on 11.12.2014.
 */
public class AppInfo {

    private Drawable icon;
    private String label;
    private String packageName;
    private String version;
    private String instVersion;
    private String path;
    private long size;
    private long firstInstallTime;
    private long lastUpdateTime;
    private Intent launchIntent;

    public AppInfo(Drawable icon, String label, String packageName,
                   String version, String instVersion, String path, long size,
                   long firstInstallTime, long lastUpdateTime, Intent launchIntent) {
        this.icon = icon;
        this.label = label;
        this.packageName = packageName;
        this.version = version;
        this.instVersion = instVersion;
        this.path = path;
        this.size = size;
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime = lastUpdateTime;
        this.launchIntent = launchIntent;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersion() {
        return version;
    }

    public String getInstalledVersion() {
        return instVersion;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getFirstInstallTime() {
        return firstInstallTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public Intent getLaunchIntent() {
        return launchIntent;
    }
}
