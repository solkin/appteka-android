package com.tomclaw.appsend.net;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.List;

public class CheckUpdatesResponse implements Parcelable, Unobfuscatable {

    @SerializedName(value = "entries")
    private List<AppEntry> entries;

    public CheckUpdatesResponse() {
    }

    public List<AppEntry> getEntries() {
        return entries;
    }

    protected CheckUpdatesResponse(Parcel in) {
        entries = in.createTypedArrayList(AppEntry.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(entries);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CheckUpdatesResponse> CREATOR = new Creator<CheckUpdatesResponse>() {
        @Override
        public CheckUpdatesResponse createFromParcel(Parcel in) {
            return new CheckUpdatesResponse(in);
        }

        @Override
        public CheckUpdatesResponse[] newArray(int size) {
            return new CheckUpdatesResponse[size];
        }
    };
}
