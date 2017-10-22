package com.tomclaw.appsend.main.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 15.10.17.
 */
public class RateItem implements Parcelable, Unobfuscatable {

    private int rate_id;
    private int score;
    private String text;
    private long time;
    private long user_id;

    public RateItem() {
    }

    protected RateItem(Parcel in) {
        rate_id = in.readInt();
        score = in.readInt();
        text = in.readString();
        time = in.readLong();
        user_id = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rate_id);
        dest.writeInt(score);
        dest.writeString(text);
        dest.writeLong(time);
        dest.writeLong(user_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RateItem> CREATOR = new Creator<RateItem>() {
        @Override
        public RateItem createFromParcel(Parcel in) {
            return new RateItem(in);
        }

        @Override
        public RateItem[] newArray(int size) {
            return new RateItem[size];
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
}
