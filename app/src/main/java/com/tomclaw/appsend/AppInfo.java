package com.tomclaw.appsend;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Solkin on 11.12.2014.
 */
public class AppInfo extends BaseItem implements Parcelable {

    public static final int FLAG_INSTALLED_APP = 0x0001;
    public static final int FLAG_APK_FILE = 0x0010;
    public static final int FLAG_DONATE_ITEM = 0x0100;
    public static final int FLAG_COUCH_ITEM = 0x1000;

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

    protected AppInfo(Parcel in) {
        label = in.readString();
        packageName = in.readString();
        version = in.readString();
        instVersion = in.readString();
        path = in.readString();
        size = in.readLong();
        firstInstallTime = in.readLong();
        lastUpdateTime = in.readLong();
        launchIntent = in.readParcelable(Intent.class.getClassLoader());
        flags = in.readInt();
    }

    public static final Creator<AppInfo> CREATOR = new Creator<AppInfo>() {
        @Override
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        @Override
        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeString(packageName);
        dest.writeString(version);
        dest.writeString(instVersion);
        dest.writeString(path);
        dest.writeLong(size);
        dest.writeLong(firstInstallTime);
        dest.writeLong(lastUpdateTime);
        dest.writeParcelable(launchIntent, flags);
        dest.writeInt(flags);
    }

    @Override
    public int getType() {
        boolean donateItem = (getFlags() & AppInfo.FLAG_DONATE_ITEM) == AppInfo.FLAG_DONATE_ITEM;
        if (donateItem) {
            return DONATE_ITEM;
        }
        boolean couchItem = (getFlags() & AppInfo.FLAG_COUCH_ITEM) == AppInfo.FLAG_COUCH_ITEM;
        if (couchItem) {
            return COUCH_ITEM;
        }
        boolean apkItem = (getFlags() & AppInfo.FLAG_APK_FILE) == AppInfo.FLAG_APK_FILE;
        return apkItem ? APK_ITEM : APP_ITEM;
    }
}
