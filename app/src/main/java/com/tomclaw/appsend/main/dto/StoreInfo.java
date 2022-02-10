package com.tomclaw.appsend.main.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.meta.Meta;
import com.tomclaw.appsend.main.ratings.UserRating;
import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.List;

/**
 * Created by ivsolkin on 17.01.17.
 */
public class StoreInfo implements Parcelable, Unobfuscatable {

    @SerializedName("expires_in")
    private long expiresIn;
    public StoreItem info;
    public String link;
    public String url;
    public int status;
    public List<StoreVersion> versions;
    public Meta meta;
    public List<RatingItem> rates;
    @SerializedName("user_rating")
    public UserRating userRating;
    public String[] actions;
    @SerializedName("topic_id")
    private int topicId;

    public StoreInfo() {
    }

    protected StoreInfo(Parcel in) {
        expiresIn = in.readLong();
        info = in.readParcelable(StoreItem.class.getClassLoader());
        link = in.readString();
        status = in.readInt();
        versions = in.createTypedArrayList(StoreVersion.CREATOR);
        meta = in.readParcelable(Meta.class.getClassLoader());
        rates = in.createTypedArrayList(RatingItem.CREATOR);
        userRating = in.readParcelable(UserRating.class.getClassLoader());
        actions = in.createStringArray();
        topicId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(expiresIn);
        dest.writeParcelable(info, flags);
        dest.writeString(link);
        dest.writeInt(status);
        dest.writeTypedList(versions);
        dest.writeParcelable(meta, flags);
        dest.writeTypedList(rates);
        dest.writeParcelable(userRating, flags);
        dest.writeStringArray(actions);
        dest.writeInt(topicId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public StoreItem getItem() {
        return info;
    }

    public String getUrl() {
        return url;
    }

    public String getLink() {
        return link;
    }

    public int getStatus() {
        return status;
    }

    public List<StoreVersion> getVersions() {
        return versions;
    }

    public Meta getMeta() {
        return meta;
    }

    public List<RatingItem> getRates() {
        return rates;
    }

    public UserRating getUserRating() {
        return userRating;
    }

    public String[] getActions() {
        return actions;
    }

    public int getTopicId() {
        return topicId;
    }

    public static final Creator<StoreInfo> CREATOR = new Creator<StoreInfo>() {
        @Override
        public StoreInfo createFromParcel(Parcel in) {
            return new StoreInfo(in);
        }

        @Override
        public StoreInfo[] newArray(int size) {
            return new StoreInfo[size];
        }
    };
}
