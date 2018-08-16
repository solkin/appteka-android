package com.tomclaw.appsend.main.store;

import android.os.Parcel;

import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.StateHolder;

import java.util.ArrayList;

public class StoreItemsState extends StateHolder.State {

    private ArrayList<StoreItem> items;
    private boolean isError;
    private boolean isLoading;
    private boolean isLoadedAll;

    public StoreItemsState() {
    }

    public StoreItemsState(ArrayList<StoreItem> items,
                           boolean isError,
                           boolean isLoading,
                           boolean isLoadedAll) {
        this.items = items;
        this.isError = isError;
        this.isLoading = isLoading;
        this.isLoadedAll = isLoadedAll;
    }

    protected StoreItemsState(Parcel in) {
        items = in.createTypedArrayList(StoreItem.CREATOR);
        isError = in.readByte() != 0;
        isLoading = in.readByte() != 0;
        isLoadedAll = in.readByte() != 0;
    }

    public ArrayList<StoreItem> getItems() {
        return items;
    }

    public boolean isError() {
        return isError;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean isLoadedAll() {
        return isLoadedAll;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(items);
        dest.writeByte((byte) (isError ? 1 : 0));
        dest.writeByte((byte) (isLoading ? 1 : 0));
        dest.writeByte((byte) (isLoadedAll ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StoreItemsState> CREATOR = new Creator<StoreItemsState>() {
        @Override
        public StoreItemsState createFromParcel(Parcel in) {
            return new StoreItemsState(in);
        }

        @Override
        public StoreItemsState[] newArray(int size) {
            return new StoreItemsState[size];
        }
    };

}
