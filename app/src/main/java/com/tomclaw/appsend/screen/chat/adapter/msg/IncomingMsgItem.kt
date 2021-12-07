package com.tomclaw.appsend.screen.chat.adapter.msg

import android.os.Parcel
import android.os.Parcelable
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import com.tomclaw.appsend.screen.chat.adapter.MsgItem

class IncomingMsgItem(
    id: Long,
    topicId: Int,
    msgId: Int,
    prevMsgId: Int,
    userId: Int,
    userIcon: UserIcon,
    text: String,
    time: Long,
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
        writeInt(type)
        attachment.writeToParcel(dest, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<IncomingMsgItem> {
        override fun createFromParcel(parcel: Parcel): IncomingMsgItem {
            val id = parcel.readLong()
            val topicId = parcel.readInt()
            val msgId = parcel.readInt()
            val prevMsgId = parcel.readInt()
            val userId = parcel.readInt()
            val userIcon = UserIcon.createFromParcel(parcel)
            val text = parcel.readString().orEmpty()
            val time = parcel.readLong()
            val type = parcel.readInt()
            val attachment = MsgAttachment.createFromParcel(parcel)
            return IncomingMsgItem(
                id,
                topicId,
                msgId,
                prevMsgId,
                userId,
                userIcon,
                text,
                time,
                type,
                attachment
            )
        }

        override fun newArray(size: Int): Array<IncomingMsgItem?> {
            return arrayOfNulls(size)
        }
    }

}
