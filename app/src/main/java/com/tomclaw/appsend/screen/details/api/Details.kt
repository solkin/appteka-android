package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
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
) : Parcelable

const val ACTION_UNLINK = "unlink"
const val ACTION_UNPUBLISH = "unpublish"
const val ACTION_DELETE = "delete"
const val ACTION_EDIT_META = "edit_meta"
