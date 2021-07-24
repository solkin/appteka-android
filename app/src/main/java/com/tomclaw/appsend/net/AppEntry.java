package com.tomclaw.appsend.net;

import com.google.gson.annotations.SerializedName;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

public class AppEntry implements Parcelable, Unobfuscatable {

    @SerializedName(value = "app_id")
    private String appId;
    @SerializedName(value = "size")
    private String size;
    @SerializedName(value = "time")
    private String time;
    @SerializedName(value = "label")
    private String label;
    @SerializedName(value = "package")
    private String packageName;
    @SerializedName(value = "ver_name")
    private String verName;
    @SerializedName(value = "ver_code")
    private String verCode;
    @SerializedName(value = "downloads")
    private String downloads;
    @SerializedName(value = "user_id")
    private String userId;
    @SerializedName(value = "icon")
    private String icon;

    public AppEntry() {
    }

    protected AppEntry(Parcel in) {
        appId = in.readString();
        size = in.readString();
        time = in.readString();
        label = in.readString();
        packageName = in.readString();
        verName = in.readString();
        verCode = in.readString();
        downloads = in.readString();
        userId = in.readString();
        icon = in.readString();
    }

    public String getAppId() {
        return appId;
    }

    public String getSize() {
        return size;
    }

    public String getTime() {
        return time;
    }

    public String getLabel() {
        return label;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVerName() {
        return verName;
    }

    public String getVerCode() {
        return verCode;
    }

    public String getDownloads() {
        return downloads;
    }

    public String getUserId() {
        return userId;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appId);
        dest.writeString(size);
        dest.writeString(time);
        dest.writeString(label);
        dest.writeString(packageName);
        dest.writeString(verName);
        dest.writeString(verCode);
        dest.writeString(downloads);
        dest.writeString(userId);
        dest.writeString(icon);
    }

    public static final Creator<AppEntry> CREATOR = new Creator<AppEntry>() {
        @Override
        public AppEntry createFromParcel(Parcel in) {
            return new AppEntry(in);
        }

        @Override
        public AppEntry[] newArray(int size) {
            return new AppEntry[size];
        }
    };
}
