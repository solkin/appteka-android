package com.tomclaw.appsend.screen.downloads.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity

class DownloadsResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)
