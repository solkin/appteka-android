package com.tomclaw.appsend;

import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Created by Solkin on 11.12.2014.
 */
public class AppInfo {

    public static final int FLAG_INSTALLED_APP = 0x0001;
    public static final int FLAG_APK_FILE = 0x0010;
    public static final int FLAG_DONATE_ITEM = 0x0100;

    private final String label;
    private final String packageName;
    private final String version;
    private final String instVersion;
    private final String path;
    private final long size;
    private final long firstInstallTime;
    private final long lastUpdateTime;
    private final Intent launchIntent;
    private final int flags;

    public AppInfo(String label, String packageName,
                   String version, String instVersion, String path, long size,
                   long firstInstallTime, long lastUpdateTime, Intent launchIntent,
                   int flags) {
        this.label = label;
        this.packageName = packageName;
        this.version = version;
        this.instVersion = instVersion;
        this.path = path;
        this.size = size;
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime = lastUpdateTime;
        this.launchIntent = launchIntent;
        this.flags = flags;
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

    public int getFlags() {
        return flags;
    }
}
