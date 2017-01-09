package com.tomclaw.appsend.main.item;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ivsolkin on 09.01.17.
 */
public class DonateItem extends BaseItem implements Parcelable {

    public DonateItem() {
    }

    protected DonateItem(Parcel in) {
    }

    public static final Creator<DonateItem> CREATOR = new Creator<DonateItem>() {
        @Override
        public DonateItem createFromParcel(Parcel in) {
            return new DonateItem(in);
        }

        @Override
        public DonateItem[] newArray(int size) {
            return new DonateItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int getType() {
        return DONATE_ITEM;
    }
}
