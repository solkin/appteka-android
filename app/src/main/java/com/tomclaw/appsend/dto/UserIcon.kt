package com.tomclaw.appsend.dto

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class UserIcon(
    @SerializedName("icon")
    val icon: String,
    @SerializedName("label")
    val label: Map<String, String>,
    @SerializedName("color")
    val color: String,
) : Parcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(icon)
        writeInt(label.size)
        for ((locale, value) in label) {
            writeString(locale)
            writeString(value)
        }
        writeString(color)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<UserIcon> {
        override fun createFromParcel(parcel: Parcel): UserIcon {
            val icon = parcel.readString().orEmpty()
            val label = HashMap<String, String>()
            val labelsCount = parcel.readInt()
            for (i in 0..labelsCount) {
                val locale = parcel.readString().orEmpty()
                val value = parcel.readString().orEmpty()
                label[locale] = value
            }
            val color = parcel.readString().orEmpty()
            return UserIcon(icon, label, color)
        }

        override fun newArray(size: Int): Array<UserIcon?> {
            return arrayOfNulls(size)
        }
    }

}
