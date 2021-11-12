package com.tomclaw.appsend.main.profile;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.util.Unobfuscatable;

public class EliminateUserResponse implements Parcelable, Unobfuscatable {

    @SerializedName("files_count")
    private int filesCount;
    @SerializedName("msgs_count")
    private int msgsCount;
    @SerializedName("ratings_count")
    private int ratingsCount;

    protected EliminateUserResponse(Parcel in) {
        filesCount = in.readInt();
        msgsCount = in.readInt();
        ratingsCount = in.readInt();
    }

    public int getFilesCount() {
        return filesCount;
    }

    public int getMessagesCount() {
        return msgsCount;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(filesCount);
        dest.writeInt(msgsCount);
        dest.writeInt(ratingsCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EliminateUserResponse> CREATOR = new Creator<EliminateUserResponse>() {
        @Override
        public EliminateUserResponse createFromParcel(Parcel in) {
            return new EliminateUserResponse(in);
        }

        @Override
        public EliminateUserResponse[] newArray(int size) {
            return new EliminateUserResponse[size];
        }
    };
}
