package com.tomclaw.appsend.main.unlink;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 19/03/2018.
 */
public class UnlinkResponse implements Parcelable, Unobfuscatable {

    private int status;

    protected UnlinkResponse(Parcel in) {
        status = in.readInt();
    }

    public int getStatus() {
        return status;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UnlinkResponse> CREATOR = new Creator<UnlinkResponse>() {
        @Override
        public UnlinkResponse createFromParcel(Parcel in) {
            return new UnlinkResponse(in);
        }

        @Override
        public UnlinkResponse[] newArray(int size) {
            return new UnlinkResponse[size];
        }
    };
}
