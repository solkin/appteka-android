package com.tomclaw.appsend.screen.chat.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class MsgTranslateResponse(
    @SerializedName("text")
    val text: String,
    @SerializedName("lang")
    val lang: String,
)
