package com.tomclaw.appsend.main.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by solkin on 23.09.17.
 */
public class Category implements Parcelable, Unobfuscatable {

    private String icon;
    private int id;
    private Map<String, String> name;

    public Category() {
    }

    protected Category(Parcel in) {
        icon = in.readString();
        id = in.readInt();
        name = new HashMap<>();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            String value = in.readString();
            name.put(key,value);
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(icon);
        dest.writeInt(id);

        dest.writeInt(name.size());
        for(Map.Entry<String,String> entry : name.entrySet()){
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public String getIcon() {
        return icon;
    }

    public int getId() {
        return id;
    }

    public Map<String, String> getNames() {
        return name;
    }

    public String getName(String locale) {
        return name.get(locale);
    }

    public String getDefaultName() {
        return name.get("en");
    }
}
