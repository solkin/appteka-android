package com.tomclaw.appsend;

import android.graphics.drawable.Drawable;

/**
 * Created by Solkin on 11.12.2014.
 */
public class AppInfo {

    private Drawable icon;
    private String label;
    private String packageName;
    private String version;
    private String path;
    private long size;
    private long lastUpdateTime;

    public AppInfo(Drawable icon, String label, String packageName, String version, String path, long size, long lastUpdateTime) {
        this.icon = icon;
        this.label = label;
        this.packageName = packageName;
        this.version = version;
        this.path = path;
        this.size = size;
        this.lastUpdateTime = lastUpdateTime;
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

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
