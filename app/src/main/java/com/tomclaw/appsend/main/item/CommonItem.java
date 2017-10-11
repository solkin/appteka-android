package com.tomclaw.appsend.main.item;

import android.content.pm.PackageInfo;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ivsolkin on 09.01.17.
 */
public abstract class CommonItem extends BaseItem implements Parcelable {

    private final String label;
    private final String packageName;
    private final String version;
    private final String path;
    private final long size;
    private final PackageInfo packageInfo;

    public CommonItem(String label, String packageName, String version, String path, long size,
                      PackageInfo packageInfo) {
        this.label = label;
        this.packageName = packageName;
        this.version = version;
        this.path = path;
        this.size = size;
        this.packageInfo = packageInfo;
    }

    protected CommonItem(Parcel in) {
        label = in.readString();
        packageName = in.readString();
        version = in.readString();
        path = in.readString();
        size = in.readLong();
        packageInfo = in.readParcelable(PackageInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeString(packageName);
        dest.writeString(version);
        dest.writeString(path);
        dest.writeLong(size);
        dest.writeParcelable(packageInfo, flags);
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

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }
}
