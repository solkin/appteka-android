package com.tomclaw.appsend.main.local;

import android.os.Parcel;
import android.os.Parcelable;

public class DialogData implements Parcelable {

    private final String title;
    private final String message;

    public DialogData(String title, String message) {
        this.title = title;
        this.message = message;
    }

    protected DialogData(Parcel in) {
        title = in.readString();
        message = in.readString();
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DialogData> CREATOR = new Creator<DialogData>() {
        @Override
        public DialogData createFromParcel(Parcel in) {
            return new DialogData(in);
        }

        @Override
        public DialogData[] newArray(int size) {
            return new DialogData[size];
        }
    };
}
