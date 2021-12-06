package com.tomclaw.appsend.screen.chat.adapter.msg

import android.os.Parcel
import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.readBool
import com.tomclaw.appsend.util.writeBool

data class MsgItem(
    override val id: Long,
    val topicId: Int,
    val msgId: Int,
    val prevMsgId: Int,
    val userId: Int,
    val userIcon: UserIcon,
    val text: String,
    val time: Long,
    val cookie: String,
    val type: Int,
    val attachment: MsgAttachment,
    val incoming: Boolean
) : Item, Parcelable {

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
        writeBool(incoming)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<MsgItem> {
        override fun createFromParcel(parcel: Parcel): MsgItem {
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
            val incoming = parcel.readBool()
            return MsgItem(
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
                attachment,
                incoming
            )
        }

        override fun newArray(size: Int): Array<MsgItem?> {
            return arrayOfNulls(size)
        }
    }

}
