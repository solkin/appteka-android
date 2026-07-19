package com.tomclaw.appsend.util

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

fun Parcel.writeBool(value: Boolean) {
    writeInt(if (value) 1 else 0)
}

fun Parcel.readBool(): Boolean {
    return readInt() == 1
}

/**
 * The API 33 typed overloads validate the CREATOR through
 * `creator.getClass().getEnclosingClass()`, which R8 can leave null after merging the
 * classes @Parcelize generates. Parcel then throws NPE instead of returning null, and an
 * intent extra read that way takes the whole process down. The untyped overloads skip
 * that check entirely, so they remain a working fallback.
 */
private inline fun <T> readParcelableCompat(typed: () -> T?, untyped: () -> T?): T? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) typed() else untyped()
    } catch (e: Exception) {
        try {
            untyped()
        } catch (e: Exception) {
            null
        }
    }
}

fun <T : Parcelable> Bundle.getParcelableCompat(key: String, clazz: Class<T>): T? {
    classLoader = clazz.classLoader
    return readParcelableCompat(
        typed = { getParcelable(key, clazz) },
        untyped = { @Suppress("DEPRECATION") getParcelable(key) },
    )
}

fun <T : Parcelable> Bundle.getParcelableArrayListCompat(
    key: String,
    clazz: Class<T>
): ArrayList<T>? {
    classLoader = clazz.classLoader
    return readParcelableCompat(
        typed = { getParcelableArrayList(key, clazz) },
        untyped = { @Suppress("DEPRECATION") getParcelableArrayList(key) },
    )
}

fun <T : Parcelable> Intent.getParcelableExtraCompat(key: String, clazz: Class<T>): T? {
    setExtrasClassLoader(clazz.classLoader)
    return readParcelableCompat(
        typed = { getParcelableExtra(key, clazz) },
        untyped = { @Suppress("DEPRECATION") getParcelableExtra(key) },
    )
}

fun <T : Parcelable> Intent.getParcelableArrayListCompat(
    key: String,
    clazz: Class<T>
): ArrayList<T>? {
    setExtrasClassLoader(clazz.classLoader)
    return readParcelableCompat(
        typed = { getParcelableArrayListExtra(key, clazz) },
        untyped = { @Suppress("DEPRECATION") getParcelableArrayListExtra(key) },
    )
}
