package com.tomclaw.appsend.main.unpublish;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 19/03/2018.
 */
public class UnpublishResponse implements Parcelable, Unobfuscatable {

    protected UnpublishResponse(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UnpublishResponse> CREATOR = new Creator<UnpublishResponse>() {
        @Override
        public UnpublishResponse createFromParcel(Parcel in) {
            return new UnpublishResponse(in);
        }

        @Override
        public UnpublishResponse[] newArray(int size) {
            return new UnpublishResponse[size];
        }
    };
}
