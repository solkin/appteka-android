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
    @SerializedName("feed")
    val feed: Int,
    @SerializedName("moderation")
    val moderation: ModerationData?,
    @SerializedName("bdui")
    val bdui: StartupBdui?,
) : Parcelable

/**
 * BDUI screen configuration from startup response.
 * When present, the app should open a BDUI screen with the specified parameters.
 */
@Parcelize
data class StartupBdui(
    @SerializedName("url")
    val url: String,
    @SerializedName("title")
    val title: String?,
) : Parcelable
