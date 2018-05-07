package com.tomclaw.appsend.main.profile.list;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.List;

/**
 * Created by solkin on 19/03/2018.
 */
public class ListResponse implements Parcelable, Unobfuscatable {

    private int status;
    private List<StoreItem> files;

    protected ListResponse(Parcel in) {
        status = in.readInt();
        files = in.createTypedArrayList(StoreItem.CREATOR);
    }

    public int getStatus() {
        return status;
    }

    public List<StoreItem> getFiles() {
        return files;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
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
