package com.tomclaw.appsend.screen.store.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.GsonModel

@GsonModel
class AppsListResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)