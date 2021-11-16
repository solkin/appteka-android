package com.tomclaw.appsend.main.auth;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.dto.UserIcon;
import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 18/12/2018.
 */
public class AuthResponse implements Parcelable, Unobfuscatable {

    private String guid;
    private long user_id;
    private UserIcon user_icon;
    private int role;
    private String email;
    private String name;
    private String description;

    protected AuthResponse(Parcel in) {
        guid = in.readString();
        user_id = in.readLong();
        user_icon = in.readParcelable(UserIcon.class.getClassLoader());
        role = in.readInt();
        email = in.readString();
        name = in.readString();
        description = in.readString();
    }

    public String getGuid() {
        return guid;
    }

    public long getUserId() {
        return user_id;
    }

    public UserIcon getUserIcon() {
        return user_icon;
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
        dest.writeString(guid);
        dest.writeLong(user_id);
        dest.writeParcelable(user_icon, 0);
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
