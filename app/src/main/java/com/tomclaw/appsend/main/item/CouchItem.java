package com.tomclaw.appsend.main.item;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ivsolkin on 09.01.17.
 */
public class CouchItem extends BaseItem implements Parcelable {

    private String couchText;
    private String buttonText;

    public CouchItem(String couchText, String buttonText) {
        this.couchText = couchText;
        this.buttonText = buttonText;
    }

    private CouchItem(Parcel in) {
        couchText = in.readString();
        buttonText = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(couchText);
        dest.writeString(buttonText);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CouchItem> CREATOR = new Creator<CouchItem>() {
        @Override
        public CouchItem createFromParcel(Parcel in) {
            return new CouchItem(in);
        }

        @Override
        public CouchItem[] newArray(int size) {
            return new CouchItem[size];
        }
    };

    @Override
    public int getType() {
        return COUCH_ITEM;
    }

    public String getCouchText() {
        return couchText;
    }

    public String getButtonText() {
        return buttonText;
    }
}
