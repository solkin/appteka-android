package com.tomclaw.appsend.main.local;

import android.os.Parcel;

import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.util.states.State;

import java.util.ArrayList;

public class ApkItemsState extends State {

    private ArrayList<ApkItem> items;

    public ApkItemsState() {
    }

    public ApkItemsState(ArrayList<ApkItem> items) {
        this.items = items;
    }

    protected ApkItemsState(Parcel in) {
        items = in.createTypedArrayList(ApkItem.CREATOR);
    }

    public ArrayList<ApkItem> getItems() {
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

    public static final Creator<ApkItemsState> CREATOR = new Creator<ApkItemsState>() {
        @Override
        public ApkItemsState createFromParcel(Parcel in) {
            return new ApkItemsState(in);
        }

        @Override
        public ApkItemsState[] newArray(int size) {
            return new ApkItemsState[size];
        }
    };

}
