package com.tomclaw.appsend.screen.discuss.adapter.topic

import android.os.Parcel
import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.util.readBool
import com.tomclaw.appsend.util.writeBool

class TopicItem(
    override val id: Long,
    val appId: String,
    val icon: String?,
    val title: String,
    val version: String,
    val size: String,
    val rating: Float,
    val downloads: Int,
    var hasMore: Boolean = false,
    var hasError: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeString(appId)
        writeString(icon)
        writeString(title)
        writeString(version)
        writeString(size)
        writeFloat(rating)
        writeInt(downloads)
        writeBool(hasMore)
        writeBool(hasError)
        writeBool(hasProgress)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TopicItem> {
        override fun createFromParcel(parcel: Parcel): TopicItem {
            val id = parcel.readLong()
            val appId = parcel.readString().orEmpty()
            val icon = parcel.readString()
            val title = parcel.readString().orEmpty()
            val version = parcel.readString().orEmpty()
            val size = parcel.readString().orEmpty()
            val rating = parcel.readFloat()
            val downloads = parcel.readInt()
            val hasMore = parcel.readBool()
            val hasError = parcel.readBool()
            val hasProgress = parcel.readBool()
            return TopicItem(
                id,
                appId,
                icon,
                title,
                version,
                size,
                rating,
                downloads,
                hasMore,
                hasError,
                hasProgress
            )
        }

        override fun newArray(size: Int): Array<TopicItem?> {
            return arrayOfNulls(size)
        }
    }

}
