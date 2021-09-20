package com.tomclaw.appsend.screen.moderation.adapter.app

import android.os.Parcel
import android.os.Parcelable
import com.avito.konveyor.blueprint.Item

class AppItem(
    override val id: Long,
    val icon: String?,
    val title: String,
    val version: String,
    val size: String,
    val rating: Float,
    val downloads: Int
) : Item, Parcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeString(icon)
        writeString(title)
        writeString(version)
        writeString(size)
        writeFloat(rating)
        writeInt(downloads)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AppItem> {
        override fun createFromParcel(parcel: Parcel): AppItem {
            val id = parcel.readLong()
            val icon = parcel.readString()
            val title = parcel.readString().orEmpty()
            val version = parcel.readString().orEmpty()
            val size = parcel.readString().orEmpty()
            val rating = parcel.readFloat()
            val downloads = parcel.readInt()
            return AppItem(id, icon, title, version, size, rating, downloads)
        }

        override fun newArray(size: Int): Array<AppItem?> {
            return arrayOfNulls(size)
        }
    }

}
