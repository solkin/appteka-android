package com.tomclaw.appsend.screen.favorite.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity

class FavoriteResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)
