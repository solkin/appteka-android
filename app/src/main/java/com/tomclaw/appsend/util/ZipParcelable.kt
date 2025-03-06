package com.tomclaw.appsend.util

import android.os.Parcel
import android.os.Parcelable

class ZipParcelable : Parcelable {

    private var raw: ByteArray? = null
    private var zip: Boolean? = null
    private var nested: Parcelable? = null

    constructor(nested: Parcelable?) {
        this.nested = nested
    }

    private constructor(raw: ByteArray, zip: Boolean) {
        this.raw = raw
        this.zip = zip
    }

    private fun writeNestedParcel(nested: Parcelable, parcel: Parcel, flags: Int) {
        parcel.writeParcelable(nested, flags)
    }

    private fun <P : Parcelable> readNestedParcel(clazz: Class<P>, parcel: Parcel): Parcelable? {
        @Suppress("DEPRECATION")
        return parcel.readParcelable(clazz.classLoader)
    }

    inline fun <reified T : Parcelable> restore() = restore(T::class.java) as? T

    fun <P : Parcelable> restore(clazz: Class<P>): Parcelable? {
        if (nested != null) return nested as Parcelable

        val data = raw?.takeIf { it.isNotEmpty() } ?: return null

        val array = when (zip == true) {
            true -> try {
                data.unzip()
            } catch (_: Throwable) {
                return null
            }

            else -> data
        }
        return readNestedParcel(clazz, array.unmarshallToParcel())
    }

    override fun writeToParcel(out: Parcel, flags: Int) = with(out) {
        when (val nested = nested) {
            null -> writeBool(false)
            else -> {
                writeBool(value = true)
                val originalArray = parcelableToByteArray { writeNestedParcel(nested, it, flags) }
                try {
                    val zip = originalArray.zip()
                    writeBool(value = true)
                    writeByteArrayWithSize(zip)
                } catch (_: Throwable) {
                    writeBool(value = false)
                    writeByteArrayWithSize(originalArray)
                }
            }
        }
    }

    private fun Parcel.writeByteArrayWithSize(array: ByteArray) {
        writeInt(array.size)
        writeByteArray(array)
    }

    override fun describeContents() = 0

    companion object {

        @JvmField
        val CREATOR = Parcels.creator {
            create(this) { raw, zip ->
                ZipParcelable(raw, zip)
            }
        }

        @JvmStatic
        fun create(
            parcel: Parcel,
            creator: (ByteArray, Boolean) -> ZipParcelable
        ): ZipParcelable = with(parcel) {
            var zip = false
            val data = when (readBool()) {
                true -> {
                    zip = readBool()
                    val size = readInt()
                    ByteArray(size).apply {
                        readByteArray(this)
                    }
                }

                else -> ByteArray(size = 0)
            }
            return creator(data, zip)
        }
    }
}

fun parcelableToByteArray(writer: (Parcel) -> Unit): ByteArray {
    val parcel = Parcel.obtain()
    writer(parcel)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}
