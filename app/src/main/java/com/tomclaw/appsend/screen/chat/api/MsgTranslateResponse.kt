package com.tomclaw.appsend.screen.chat.api

import com.google.gson.annotations.SerializedName

data class MsgTranslateResponse(
    @SerializedName("text")
    val text: String,
    @SerializedName("lang")
    val lang: String,
)
