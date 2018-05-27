package com.tomclaw.appsend.main.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by ivsolkin on 17.01.17.
 */

public class StoreVersion implements Parcelable, Unobfuscatable {

    @SerializedName("app_id")
    private String appId;
    private int downloads;
    @SerializedName("ver_code")
    private int verCode;
    @SerializedName("ver_name")
    private String verName;

    public StoreVersion(String appId, int downloads, int verCode, String verName) {
        this.appId = appId;
        this.downloads = downloads;
        this.verCode = verCode;
        this.verName = verName;
    }

    protected StoreVersion(Parcel in) {
        appId = in.readString();
        downloads = in.readInt();
        verCode = in.readInt();
        verName = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(appId);
        dest.writeInt(downloads);
        dest.writeInt(verCode);
        dest.writeString(verName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getAppId() {
        return appId;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getVerCode() {
        return verCode;
    }

    public String getVerName() {
        return verName;
    }

    public static final Creator<StoreVersion> CREATOR = new Creator<StoreVersion>() {
        @Override
        public StoreVersion createFromParcel(Parcel in) {
            return new StoreVersion(in);
        }

        @Override
        public StoreVersion[] newArray(int size) {
            return new StoreVersion[size];
        }
    };
}
