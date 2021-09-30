package com.tomclaw.appsend.util

import android.os.Parcel

fun Parcel.writeBool(value: Boolean) {
    writeInt(if (value) 1 else 0)
}

fun Parcel.readBool(): Boolean {
    return readInt() == 1
}