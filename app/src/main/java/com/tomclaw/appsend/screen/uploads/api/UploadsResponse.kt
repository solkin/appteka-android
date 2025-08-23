package com.tomclaw.appsend.screen.uploads.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity

class UploadsResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)
