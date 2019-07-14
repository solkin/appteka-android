package com.tomclaw.appsend.main.ratings;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.main.dto.RatingItem;
import com.tomclaw.appsend.util.Unobfuscatable;

import java.util.List;

/**
 * Created by solkin on 23.09.17.
 */
public class RatingsResponse implements Parcelable, Unobfuscatable {

    @SerializedName("rating")
    private float rating;
    @SerializedName("count")
    private int rate_count;
    @SerializedName("entries")
    private List<RatingItem> rates;

    public RatingsResponse() {
    }

    protected RatingsResponse(Parcel in) {
        rating = in.readFloat();
        rate_count = in.readInt();
        rates = in.createTypedArrayList(RatingItem.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(rating);
        dest.writeInt(rate_count);
        dest.writeTypedList(rates);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RatingsResponse> CREATOR = new Creator<RatingsResponse>() {
        @Override
        public RatingsResponse createFromParcel(Parcel in) {
            return new RatingsResponse(in);
        }

        @Override
        public RatingsResponse[] newArray(int size) {
            return new RatingsResponse[size];
        }
    };

    public float getRating() {
        return rating;
    }

    public int getRatingsCount() {
        return rate_count;
    }

    public List<RatingItem> getRatings() {
        return rates;
    }
}
