package com.tomclaw.appsend.screen.uploads.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.GsonModel

@GsonModel
class UploadsResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)
