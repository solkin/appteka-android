package com.tomclaw.appsend.main.profile.list;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.List;

/**
 * Created by solkin on 19/03/2018.
 */
public class ListResponse implements Parcelable, Unobfuscatable {

    @SerializedName("entries")
    private final List<StoreItem> files;

    protected ListResponse(Parcel in) {
        files = in.createTypedArrayList(StoreItem.CREATOR);
    }

    public List<StoreItem> getFiles() {
        return files;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(files);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ListResponse> CREATOR = new Creator<ListResponse>() {
        @Override
        public ListResponse createFromParcel(Parcel in) {
            return new ListResponse(in);
        }

        @Override
        public ListResponse[] newArray(int size) {
            return new ListResponse[size];
        }
    };
}
