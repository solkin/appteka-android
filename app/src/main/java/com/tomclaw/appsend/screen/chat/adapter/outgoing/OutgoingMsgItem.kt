package com.tomclaw.appsend.screen.chat.adapter.outgoing

import android.os.Parcel
import android.os.Parcelable
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import com.tomclaw.appsend.screen.chat.adapter.MsgItem

class OutgoingMsgItem(
    id: Long,
    topicId: Int,
    msgId: Int,
    prevMsgId: Int,
    userId: Int,
    userIcon: UserIcon,
    text: String,
    time: Long,
    val cookie: String,
    type: Int,
    attachment: MsgAttachment
) : MsgItem(id, topicId, msgId, prevMsgId, userId, userIcon, text, time, type, attachment),
    Parcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeInt(topicId)
        writeInt(msgId)
        writeInt(prevMsgId)
        writeInt(userId)
        userIcon.writeToParcel(dest, flags)
        writeString(text)
        writeLong(time)
        writeString(cookie)
        writeInt(type)
        attachment.writeToParcel(dest, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<OutgoingMsgItem> {
        override fun createFromParcel(parcel: Parcel): OutgoingMsgItem {
            val id = parcel.readLong()
            val topicId = parcel.readInt()
            val msgId = parcel.readInt()
            val prevMsgId = parcel.readInt()
            val userId = parcel.readInt()
            val userIcon = UserIcon.createFromParcel(parcel)
            val text = parcel.readString().orEmpty()
            val time = parcel.readLong()
            val cookie = parcel.readString().orEmpty()
            val type = parcel.readInt()
            val attachment = MsgAttachment.createFromParcel(parcel)
            return OutgoingMsgItem(
                id,
                topicId,
                msgId,
                prevMsgId,
                userId,
                userIcon,
                text,
                time,
                cookie,
                type,
                attachment
            )
        }

        override fun newArray(size: Int): Array<OutgoingMsgItem?> {
            return arrayOfNulls(size)
        }
    }

}
