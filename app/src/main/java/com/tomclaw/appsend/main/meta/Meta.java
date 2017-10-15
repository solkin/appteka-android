package com.tomclaw.appsend.main.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 23.09.17.
 */
public class Meta implements Parcelable, Unobfuscatable {

    private Category category;
    private String description;
    private boolean exclusive;
    private boolean similar;
    private long time;
    private long user_id;
    private int rate_count;
    private float rating;
    private Scores scores;

    public Meta() {
    }

    protected Meta(Parcel in) {
        category = in.readParcelable(Category.class.getClassLoader());
        description = in.readString();
        exclusive = in.readByte() != 0;
        similar = in.readByte() != 0;
        time = in.readLong();
        user_id = in.readLong();
        rate_count = in.readInt();
        rating = in.readFloat();
        scores = in.readParcelable(Scores.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(category, flags);
        dest.writeString(description);
        dest.writeByte((byte) (exclusive ? 1 : 0));
        dest.writeByte((byte) (similar ? 1 : 0));
        dest.writeLong(time);
        dest.writeLong(user_id);
        dest.writeInt(rate_count);
        dest.writeFloat(rating);
        dest.writeParcelable(scores, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Meta> CREATOR = new Creator<Meta>() {
        @Override
        public Meta createFromParcel(Parcel in) {
            return new Meta(in);
        }

        @Override
        public Meta[] newArray(int size) {
            return new Meta[size];
        }
    };

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public boolean isSimilar() {
        return similar;
    }

    public long getTime() {
        return time;
    }

    public long getUserId() {
        return user_id;
    }

    public int getRateCount() {
        return rate_count;
    }

    public float getRating() {
        return rating;
    }

    public Scores getScores() {
        return scores;
    }
}
