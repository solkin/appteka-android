package com.tomclaw.appsend.screen.auth.request_code.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

@GsonModel
class RequestCodeResponse(
    @SerializedName("registered")
    val registered: Boolean,
    @SerializedName("request_id")
    val requestId: String,
    @SerializedName("code_regex")
    val codeRegex: String,
    @SerializedName("name_regex")
    val nameRegex: String,
)
