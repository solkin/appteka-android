package com.tomclaw.appsend.screen.chat.adapter

import android.os.Parcel
import android.os.Parcelable

class MsgAttachment(
    val previewUrl: String,
    val originalUrl: String,
    val size: Long,
    val width: Int,
    val height: Int
) : Parcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(previewUrl)
        writeString(originalUrl)
        writeLong(size)
        writeInt(width)
        writeInt(height)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MsgAttachment> {
        override fun createFromParcel(parcel: Parcel): MsgAttachment {
            val previewUrl = parcel.readString().orEmpty()
            val originalUrl = parcel.readString().orEmpty()
            val size = parcel.readLong()
            val width = parcel.readInt()
            val height = parcel.readInt()
            return MsgAttachment(
                previewUrl,
                originalUrl,
                size,
                width,
                height
            )
        }

        override fun newArray(size: Int): Array<MsgAttachment?> {
            return arrayOfNulls(size)
        }
    }

}
