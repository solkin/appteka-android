package com.tomclaw.appsend.main.profile;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 19/03/2018.
 */
public class EmpowerResponse implements Parcelable, Unobfuscatable {

    private int status;

    protected EmpowerResponse(Parcel in) {
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

    public static final Creator<EmpowerResponse> CREATOR = new Creator<EmpowerResponse>() {
        @Override
        public EmpowerResponse createFromParcel(Parcel in) {
            return new EmpowerResponse(in);
        }

        @Override
        public EmpowerResponse[] newArray(int size) {
            return new EmpowerResponse[size];
        }
    };
}
