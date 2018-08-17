package com.tomclaw.appsend.main.item;

import android.content.pm.PackageInfo;
import android.os.Parcel;

/**
 * Created by ivsolkin on 09.01.17.
 */
public class ApkItem extends CommonItem {

    private long createTime;

    public ApkItem() {
        super();
    }

    public ApkItem(String label, String packageName, String version, String path, long size,
                   long createTime, PackageInfo packageInfo) {
        super(label, packageName, version, path, size, packageInfo);
        this.createTime = createTime;
    }

    private ApkItem(Parcel in) {
        super(in);
        createTime = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(createTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ApkItem> CREATOR = new Creator<ApkItem>() {
        @Override
        public ApkItem createFromParcel(Parcel in) {
            return new ApkItem(in);
        }

        @Override
        public ApkItem[] newArray(int size) {
            return new ApkItem[size];
        }
    };

    public long getCreateTime() {
        return createTime;
    }
}
