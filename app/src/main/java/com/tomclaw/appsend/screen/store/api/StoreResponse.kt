package com.tomclaw.appsend.screen.store.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity

class StoreResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)