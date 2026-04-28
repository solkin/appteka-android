package com.avito.android.krop.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class SizeF(var width: Float = 0.0f, var height: Float = 0.0f) : Parcelable {

    val widthInt: Int
        get() = width.toInt()

    val heightInt: Int
        get() = height.toInt()

    override fun toString(): String {
        return "SizeF(width=$widthInt, height=$heightInt)"
    }

}