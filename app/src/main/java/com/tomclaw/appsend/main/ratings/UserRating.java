package com.tomclaw.appsend.main.ratings;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 29/10/2017.
 */

public class UserRating implements Unobfuscatable, Parcelable {

    private int rate_id;
    private int score;
    private String text;
    private long time;
    private int user_id;

    public UserRating() {
    }

    protected UserRating(Parcel in) {
        rate_id = in.readInt();
        score = in.readInt();
        text = in.readString();
        time = in.readLong();
        user_id = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rate_id);
        dest.writeInt(score);
        dest.writeString(text);
        dest.writeLong(time);
        dest.writeInt(user_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserRating> CREATOR = new Creator<UserRating>() {
        @Override
        public UserRating createFromParcel(Parcel in) {
            return new UserRating(in);
        }

        @Override
        public UserRating[] newArray(int size) {
            return new UserRating[size];
        }
    };

    public int getRateId() {
        return rate_id;
    }

    public int getScore() {
        return score;
    }

    public String getText() {
        return text;
    }

    public long getTime() {
        return time;
    }

    public int getUserId() {
        return user_id;
    }
}
