package com.tomclaw.appsend.net;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.HashMap;
import java.util.Map;

public class CheckUpdatesRequest implements Parcelable, Unobfuscatable {

    @SerializedName(value = "locale")
    private String locale;
    @SerializedName(value = "apps")
    private Map<String, Integer> apps;

    public CheckUpdatesRequest() {
    }

    public CheckUpdatesRequest(String locale, Map<String, Integer> apps) {
        this.locale = locale;
        this.apps = apps;
    }

    protected CheckUpdatesRequest(Parcel in) {
        locale = in.readString();
        apps = new HashMap<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            int value = in.readInt();
            apps.put(key, value);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(locale);
        dest.writeInt(apps.size());
        for (Map.Entry<String, Integer> entry : apps.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeInt(entry.getValue());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CheckUpdatesRequest> CREATOR = new Creator<CheckUpdatesRequest>() {
        @Override
        public CheckUpdatesRequest createFromParcel(Parcel in) {
            return new CheckUpdatesRequest(in);
        }

        @Override
        public CheckUpdatesRequest[] newArray(int size) {
            return new CheckUpdatesRequest[size];
        }
    };

}
