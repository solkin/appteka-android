package com.tomclaw.appsend.screen.topics.adapter.topic

import android.os.Parcel
import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.readBool
import com.tomclaw.appsend.util.writeBool

class TopicItem(
    override val id: Long,
    val icon: String,
    val title: String,
    val description: String?,
    val packageName: String?,
    val isPinned: Boolean,
    val hasUnread: Boolean,
    val lastMsgText: String,
    val lastMsgUserIcon: UserIcon,
    var hasMore: Boolean = false,
    var hasError: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeString(icon)
        writeString(title)
        writeString(description)
        writeString(packageName)
        writeBool(isPinned)
        writeBool(hasUnread)
        writeString(lastMsgText)
        lastMsgUserIcon.writeToParcel(this, flags)
        writeBool(hasMore)
        writeBool(hasError)
        writeBool(hasProgress)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TopicItem> {
        override fun createFromParcel(parcel: Parcel): TopicItem {
            val id = parcel.readLong()
            val icon = parcel.readString().orEmpty()
            val title = parcel.readString().orEmpty()
            val description = parcel.readString()
            val packageName = parcel.readString()
            val isPinned = parcel.readBool()
            val hasUnread = parcel.readBool()
            val lastMsgText = parcel.readString().orEmpty()
            val lastMsgUserIcon = UserIcon.createFromParcel(parcel)
            val hasMore = parcel.readBool()
            val hasError = parcel.readBool()
            val hasProgress = parcel.readBool()
            return TopicItem(
                id,
                icon,
                title,
                description,
                packageName,
                isPinned,
                hasUnread,
                lastMsgText,
                lastMsgUserIcon,
                hasMore,
                hasError,
                hasProgress
            )
        }

        override fun newArray(size: Int): Array<TopicItem?> {
            return arrayOfNulls(size)
        }
    }

}
