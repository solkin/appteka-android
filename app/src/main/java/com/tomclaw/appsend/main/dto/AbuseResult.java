package com.tomclaw.appsend.main.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by solkin on 28/11/2017.
 */
public class AbuseResult implements Parcelable {

    private int status;

    public AbuseResult() {
    }

    public AbuseResult(int status) {
        this.status = status;
    }

    protected AbuseResult(Parcel in) {
        status = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AbuseResult> CREATOR = new Creator<AbuseResult>() {
        @Override
        public AbuseResult createFromParcel(Parcel in) {
            return new AbuseResult(in);
        }

        @Override
        public AbuseResult[] newArray(int size) {
            return new AbuseResult[size];
        }
    };

}
