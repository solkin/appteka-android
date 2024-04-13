package com.tomclaw.appsend.screen.home.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class StartupResponse(
    @SerializedName("update")
    val update: AppEntity?,
    @SerializedName("unread")
    val unread: Int,
    @SerializedName("moderation")
    val moderation: ModerationData,
) : Parcelable
