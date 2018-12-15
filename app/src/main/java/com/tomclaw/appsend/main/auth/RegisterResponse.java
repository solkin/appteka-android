package com.tomclaw.appsend.main.auth;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 15/12/2018.
 */
public class RegisterResponse implements Parcelable, Unobfuscatable {

    private int status;
    private String guid;
    private String description;

    protected RegisterResponse(Parcel in) {
        status = in.readInt();
        guid = in.readString();
        description = in.readString();
    }

    public int getStatus() {
        return status;
    }

    public String getGuid() {
        return guid;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeString(guid);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RegisterResponse> CREATOR = new Creator<RegisterResponse>() {
        @Override
        public RegisterResponse createFromParcel(Parcel in) {
            return new RegisterResponse(in);
        }

        @Override
        public RegisterResponse[] newArray(int size) {
            return new RegisterResponse[size];
        }
    };
}
