package com.tomclaw.appsend.main.profile;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 17/03/2018.
 */
public class Profile implements Parcelable, Unobfuscatable {

    @SerializedName("user_id")
    private int userId;
    @SerializedName("join_time")
    private long joinTime;
    @SerializedName("last_seen")
    private long lastSeen;
    @SerializedName("role")
    private int role;
    @SerializedName("mentor_id")
    private int mentorId;
    @SerializedName("files_count")
    private int filesCount;
    @SerializedName("msg_count")
    private int msgCount;
    @SerializedName("ratings_count")
    private int ratingsCount;
    @SerializedName("mentor_of_count")
    private int moderatorsCount;
    @SerializedName("name")
    private String name;
    @SerializedName("is_registered")
    private boolean isRegistered;
    @SerializedName("url")
    private String url;

    protected Profile(Parcel in) {
        userId = in.readInt();
        joinTime = in.readLong();
        lastSeen = in.readLong();
        role = in.readInt();
        mentorId = in.readInt();
        filesCount = in.readInt();
        msgCount = in.readInt();
        ratingsCount = in.readInt();
        moderatorsCount = in.readInt();
        name = in.readString();
        isRegistered = in.readInt() == 1;
        url = in.readString();
    }

    public int getUserId() {
        return userId;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public int getRole() {
        return role;
    }

    public int getMentorId() {
        return mentorId;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public int getModeratorsCount() {
        return moderatorsCount;
    }

    public String getName() {
        return name;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeLong(joinTime);
        dest.writeLong(lastSeen);
        dest.writeInt(role);
        dest.writeInt(mentorId);
        dest.writeInt(filesCount);
        dest.writeInt(msgCount);
        dest.writeInt(ratingsCount);
        dest.writeInt(moderatorsCount);
        dest.writeString(name);
        dest.writeInt(isRegistered ? 1 : 0);
        dest.writeString(url);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
