package com.tomclaw.appsend.screen.profile.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import com.tomclaw.appsend.dto.AppEntity
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
class UserAppsResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
) : Parcelable
