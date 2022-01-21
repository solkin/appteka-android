package com.tomclaw.appsend.screen.chat.api

import com.google.gson.annotations.SerializedName

data class SendMessageResponse(
    @SerializedName("time")
    val time: Long
)