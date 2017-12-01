package com.tomclaw.appsend.main.permissions;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by solkin on 01/12/2017.
 */
public class PermissionsList implements Parcelable {

    private ArrayList<String> permissons;

    public PermissionsList(ArrayList<String> permissons) {
        this.permissons = permissons;
    }

    protected PermissionsList(Parcel in) {
        permissons = in.createStringArrayList();
    }

    public boolean isEmpty() {
        return permissons == null || permissons.isEmpty();
    }

    public List<String> getList() {
        return permissons;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(permissons);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PermissionsList> CREATOR = new Creator<PermissionsList>() {
        @Override
        public PermissionsList createFromParcel(Parcel in) {
            return new PermissionsList(in);
        }

        @Override
        public PermissionsList[] newArray(int size) {
            return new PermissionsList[size];
        }
    };
}
