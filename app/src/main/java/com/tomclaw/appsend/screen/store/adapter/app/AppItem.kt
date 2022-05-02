package com.tomclaw.appsend.screen.store.adapter.app

import android.os.Parcel
import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.util.readBool
import com.tomclaw.appsend.util.writeBool

class AppItem(
    override val id: Long,
    val appId: String,
    val icon: String?,
    val title: String,
    val version: String,
    val size: String,
    val rating: Float,
    val downloads: Int,
    var isNew: Boolean = false,
    var hasMore: Boolean = false,
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
        writeBool(isNew)
        writeBool(hasMore)
        writeBool(hasProgress)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AppItem> {
        override fun createFromParcel(parcel: Parcel): AppItem {
            val id = parcel.readLong()
            val appId = parcel.readString().orEmpty()
            val icon = parcel.readString()
            val title = parcel.readString().orEmpty()
            val version = parcel.readString().orEmpty()
            val size = parcel.readString().orEmpty()
            val rating = parcel.readFloat()
            val downloads = parcel.readInt()
            val isNew = parcel.readBool()
            val hasMore = parcel.readBool()
            val hasProgress = parcel.readBool()
            return AppItem(
                id,
                appId,
                icon,
                title,
                version,
                size,
                rating,
                downloads,
                isNew,
                hasMore,
                hasProgress
            )
        }

        override fun newArray(size: Int): Array<AppItem?> {
            return arrayOfNulls(size)
        }
    }

}
