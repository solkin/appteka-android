package com.tomclaw.appsend.main.ratings;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 29/10/2017.
 */
public class VoidResponse implements Parcelable, Unobfuscatable {

    public VoidResponse() {
    }

    protected VoidResponse(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VoidResponse> CREATOR = new Creator<VoidResponse>() {
        @Override
        public VoidResponse createFromParcel(Parcel in) {
            return new VoidResponse(in);
        }

        @Override
        public VoidResponse[] newArray(int size) {
            return new VoidResponse[size];
        }
    };
}
