package com.tomclaw.appsend.main.item;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by ivsolkin on 09.01.17.
 */
public class CouchItem extends BaseItem implements Parcelable {

    private String couchText;
    private CouchButton[] buttons;

    public CouchItem(String couchText, CouchButton... buttons) {
        this.couchText = couchText;
        this.buttons = buttons;
    }

    private CouchItem(Parcel in) {
        couchText = in.readString();
        buttons = in.createTypedArray(CouchButton.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(couchText);
        dest.writeTypedArray(buttons, flags);
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

    public List<CouchButton> getButtons() {
        return Collections.unmodifiableList(Arrays.asList(buttons));
    }

    public static class CouchButton implements Parcelable {

        private String action;
        private String label;

        public CouchButton(String action, String label) {
            this.action = action;
            this.label = label;
        }

        protected CouchButton(Parcel in) {
            action = in.readString();
            label = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(action);
            dest.writeString(label);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<CouchButton> CREATOR = new Creator<CouchButton>() {
            @Override
            public CouchButton createFromParcel(Parcel in) {
                return new CouchButton(in);
            }

            @Override
            public CouchButton[] newArray(int size) {
                return new CouchButton[size];
            }
        };

        public String getAction() {
            return action;
        }

        public String getLabel() {
            return label;
        }
    }
}
