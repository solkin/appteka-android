package com.tomclaw.appsend.screen.chat.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.MessageEntity

class HistoryResponse(
    @SerializedName("messages")
    val messages: List<MessageEntity>
)