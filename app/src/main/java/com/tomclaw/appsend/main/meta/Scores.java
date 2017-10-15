package com.tomclaw.appsend.main.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created by solkin on 15.10.17.
 */

public class Scores implements Parcelable, Unobfuscatable {

    private int five;
    private int four;
    private int three;
    private int two;
    private int one;

    public Scores() {
    }

    protected Scores(Parcel in) {
        five = in.readInt();
        four = in.readInt();
        three = in.readInt();
        two = in.readInt();
        one = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(five);
        dest.writeInt(four);
        dest.writeInt(three);
        dest.writeInt(two);
        dest.writeInt(one);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Scores> CREATOR = new Creator<Scores>() {
        @Override
        public Scores createFromParcel(Parcel in) {
            return new Scores(in);
        }

        @Override
        public Scores[] newArray(int size) {
            return new Scores[size];
        }
    };

    public int getFive() {
        return five;
    }

    public int getFour() {
        return four;
    }

    public int getThree() {
        return three;
    }

    public int getTwo() {
        return two;
    }

    public int getOne() {
        return one;
    }
}
