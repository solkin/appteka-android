package com.tomclaw.appsend.screen.chat.adapter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MsgAttachment(
    val previewUrl: String,
    val originalUrl: String,
    val size: Long,
    val width: Int,
    val height: Int
) : Parcelable
