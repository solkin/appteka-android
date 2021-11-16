package com.tomclaw.appsend.main.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.dto.UserIcon;
import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 15.10.17.
 */
public class RatingItem implements Parcelable, Unobfuscatable {

    private int rate_id;
    private int score;
    private String text;
    private long time;
    private long user_id;
    private UserIcon user_icon;

    public RatingItem() {
    }

    protected RatingItem(Parcel in) {
        rate_id = in.readInt();
        score = in.readInt();
        text = in.readString();
        time = in.readLong();
        user_id = in.readLong();
        user_icon = in.readParcelable(UserIcon.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rate_id);
        dest.writeInt(score);
        dest.writeString(text);
        dest.writeLong(time);
        dest.writeLong(user_id);
        dest.writeParcelable(user_icon, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RatingItem> CREATOR = new Creator<RatingItem>() {
        @Override
        public RatingItem createFromParcel(Parcel in) {
            return new RatingItem(in);
        }

        @Override
        public RatingItem[] newArray(int size) {
            return new RatingItem[size];
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

    public long getUserId() {
        return user_id;
    }

    public UserIcon getUserIcon() {
        return user_icon;
    }
}
