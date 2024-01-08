package com.tomclaw.appsend.util

import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable

class SizeF(var width: Float = 0.0f, var height: Float = 0.0f) : Parcelable {

    val widthInt: Int
        get() = width.toInt()

    val heightInt: Int
        get() = height.toInt()

    fun middle(size: SizeF) = RectF(
        (width - size.width) / 2,
        (height - size.height) / 2,
        (width - size.width) / 2,
        (height - size.height) / 2
    )

    constructor(parcel: Parcel) : this(
        width = parcel.readFloat(),
        height = parcel.readFloat())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(width)
        parcel.writeFloat(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SizeF> {
        override fun createFromParcel(parcel: Parcel): SizeF {
            return SizeF(parcel)
        }

        override fun newArray(size: Int): Array<SizeF?> {
            return arrayOfNulls(size)
        }
    }

}
