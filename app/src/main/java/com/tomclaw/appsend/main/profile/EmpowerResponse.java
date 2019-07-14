package com.tomclaw.appsend.main.profile;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 19/03/2018.
 */
public class EmpowerResponse implements Parcelable, Unobfuscatable {

    protected EmpowerResponse(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
