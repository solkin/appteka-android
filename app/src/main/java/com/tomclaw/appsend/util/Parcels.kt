package com.tomclaw.appsend.util

import android.os.Parcel
import android.os.Parcelable

object Parcels {
    @JvmStatic
    fun <T : Parcelable> creator(body: Parcel.() -> T) = object : Parcelable.Creator<T> {
        override fun createFromParcel(source: Parcel) = body(source)

        override fun newArray(size: Int) = arrayOfNulls<Any>(size) as Array<T?>
    }

    @JvmStatic
    fun Parcel.writeBool(value: Boolean) = writeInt(if (value) 1 else 0)

    @JvmStatic
    fun Parcel.readBool(): Boolean = readInt() == 1
}
