package com.tomclaw.appsend.main.local;

import android.os.Parcel;

import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.util.states.State;

import java.util.ArrayList;

public class AppItemsState extends State {

    private ArrayList<AppItem> items;

    public AppItemsState() {
    }

    public AppItemsState(ArrayList<AppItem> items) {
        this.items = items;
    }

    protected AppItemsState(Parcel in) {
        items = in.createTypedArrayList(AppItem.CREATOR);
    }

    public ArrayList<AppItem> getItems() {
        return items;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(items);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppItemsState> CREATOR = new Creator<>() {
        @Override
        public AppItemsState createFromParcel(Parcel in) {
            return new AppItemsState(in);
        }

        @Override
        public AppItemsState[] newArray(int size) {
            return new AppItemsState[size];
        }
    };

}
