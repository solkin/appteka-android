package com.tomclaw.appsend.main.meta;

import android.os.Parcel;
import android.os.Parcelable;

import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.List;

/**
 * Created by solkin on 23.09.17.
 */
public class MetaResponse implements Parcelable, Unobfuscatable {

    private Meta meta;
    private List<Category> categories;

    public MetaResponse() {
    }

    protected MetaResponse(Parcel in) {
        meta = in.readParcelable(Meta.class.getClassLoader());
        categories = in.createTypedArrayList(Category.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(meta, flags);
        dest.writeTypedList(categories);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MetaResponse> CREATOR = new Creator<MetaResponse>() {
        @Override
        public MetaResponse createFromParcel(Parcel in) {
            return new MetaResponse(in);
        }

        @Override
        public MetaResponse[] newArray(int size) {
            return new MetaResponse[size];
        }
    };

    public Meta getMeta() {
        return meta;
    }

    public List<Category> getCategories() {
        return categories;
    }
}
