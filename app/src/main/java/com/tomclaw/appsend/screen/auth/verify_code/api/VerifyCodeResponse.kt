package com.tomclaw.appsend.screen.auth.verify_code.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.user.api.UserProfile
import com.tomclaw.appsend.util.GsonModel

@GsonModel
class VerifyCodeResponse(
    @SerializedName("user_info")
    val profile: UserProfile
)
