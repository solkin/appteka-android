package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class Details(
    @SerializedName("url")
    val url: String,
    @SerializedName("topic_id")
    val topicId: Int?,
    @SerializedName("msg_count")
    val msgCount: Int?,
    @SerializedName("actions")
    val actions: ArrayList<String>?,
    @SerializedName("rates")
    val ratingsList: ArrayList<RatingEntity>?,
    @SerializedName("user_rating")
    val userRating: RatingEntity?,
    @SerializedName("versions")
    val versions: ArrayList<AppVersion>?,
    @SerializedName("link")
    val link: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
    @SerializedName("meta")
    val meta: Meta?,
    @SerializedName("info")
    val info: AppInfo,
    @SerializedName("is_favorite")
    val isFavorite: Boolean?,
    @SerializedName("security")
    val security: Security?,
    @SerializedName("translation")
    val translation: TranslationResponse?,
    @SerializedName("developer")
    val developer: Developer?,
    @SerializedName("moderation")
    val moderation: ModerationInfo?,
    @SerializedName("capabilities")
    val capabilities: Map<String, Capability>? = null,
) : Parcelable

// Note: the legacy `actions` field on [Details] is still serialised by
// the server for backward compatibility with older clients, but this
// app no longer consumes it. All decisions are driven by
// [Details.capabilities] via [com.tomclaw.appsend.core.permissions.CapabilityPolicy].
