package com.tomclaw.appsend.main.auth;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 18/12/2018.
 */
public class AuthResponse implements Parcelable, Unobfuscatable {

    private int status;
    private String guid;
    private long user_id;
    private int role;
    private String email;
    private String name;
    private String description;

    protected AuthResponse(Parcel in) {
        status = in.readInt();
        guid = in.readString();
        user_id = in.readLong();
        role = in.readInt();
        email = in.readString();
        name = in.readString();
        description = in.readString();
    }

    public int getStatus() {
        return status;
    }

    public String getGuid() {
        return guid;
    }

    public long getUserId() {
        return user_id;
    }

    public int getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status);
        dest.writeString(guid);
        dest.writeLong(user_id);
        dest.writeInt(role);
        dest.writeString(email);
        dest.writeString(name);
        dest.writeString(description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AuthResponse> CREATOR = new Creator<AuthResponse>() {
        @Override
        public AuthResponse createFromParcel(Parcel in) {
            return new AuthResponse(in);
        }

        @Override
        public AuthResponse[] newArray(int size) {
            return new AuthResponse[size];
        }
    };
}
