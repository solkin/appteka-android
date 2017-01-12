package com.tomclaw.appsend.main.item;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Solkin on 11.12.2014.
 */
public class AppItem extends CommonItem implements Parcelable {

    private final long firstInstallTime;
    private final long lastUpdateTime;

    public AppItem(String label, String packageName, String version, String path, long size,
                   long firstInstallTime, long lastUpdateTime, PackageInfo packageInfo) {
        super(label, packageName, version, path, size, packageInfo);
        this.firstInstallTime = firstInstallTime;
        this.lastUpdateTime = lastUpdateTime;
    }

    private AppItem(Parcel in) {
        super(in);
        firstInstallTime = in.readLong();
        lastUpdateTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(firstInstallTime);
        dest.writeLong(lastUpdateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppItem> CREATOR = new Creator<AppItem>() {
        @Override
        public AppItem createFromParcel(Parcel in) {
            return new AppItem(in);
        }

        @Override
        public AppItem[] newArray(int size) {
            return new AppItem[size];
        }
    };

    @Override
    public int getType() {
        return APP_ITEM;
    }

    public long getFirstInstallTime() {
        return firstInstallTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
