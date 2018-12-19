package com.tomclaw.appsend.main.profile;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 17/03/2018.
 */
public class Profile implements Parcelable, Unobfuscatable {

    private int user_id;
    private long join_time;
    private long last_seen;
    private int role;
    private int mentor_id;
    private int files_count;
    private int msg_count;
    private int ratings_count;
    private int moderators_count;
    private String name;
    private boolean is_registered;

    protected Profile(Parcel in) {
        user_id = in.readInt();
        join_time = in.readLong();
        last_seen = in.readLong();
        role = in.readInt();
        mentor_id = in.readInt();
        files_count = in.readInt();
        msg_count = in.readInt();
        ratings_count = in.readInt();
        moderators_count = in.readInt();
        name = in.readString();
        is_registered = in.readInt() == 1;
    }

    public int getUserId() {
        return user_id;
    }

    public long getJoinTime() {
        return join_time;
    }

    public long getLastSeen() {
        return last_seen;
    }

    public int getRole() {
        return role;
    }

    public int getMentorId() {
        return mentor_id;
    }

    public int getFilesCount() {
        return files_count;
    }

    public int getMsgCount() {
        return msg_count;
    }

    public int getRatingsCount() {
        return ratings_count;
    }

    public int getModeratorsCount() {
        return moderators_count;
    }

    public String getName() {
        return name;
    }

    public boolean isRegistered() {
        return is_registered;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(user_id);
        dest.writeLong(join_time);
        dest.writeLong(last_seen);
        dest.writeInt(role);
        dest.writeInt(mentor_id);
        dest.writeInt(files_count);
        dest.writeInt(msg_count);
        dest.writeInt(ratings_count);
        dest.writeInt(moderators_count);
        dest.writeString(name);
        dest.writeInt(is_registered ? 1 : 0);
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
