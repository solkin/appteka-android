package com.tomclaw.appsend.screen.profile.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.reviews.api.ReviewEntity
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class Profile(
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("email")
    val email: String? = null,
    @SerializedName("name_regex")
    val nameRegex: String? = null,
    @SerializedName("user_icon")
    val userIcon: UserIcon,
    @SerializedName("join_time")
    val joinTime: Long = 0,
    @SerializedName("last_seen")
    val lastSeen: Long = 0,
    @SerializedName("role")
    val role: Int = 0,
    @SerializedName("mentor_id")
    val mentorId: Int = 0,
    @SerializedName("files_count")
    val filesCount: Int = 0,
    @SerializedName("favorites_count")
    val favoritesCount: Int = 0,
    @SerializedName("downloads_count")
    val downloadsCount: Int? = 0,
    @SerializedName("total_downloads")
    val totalDownloads: Int = 0,
    @SerializedName("msg_count")
    val msgCount: Int = 0,
    @SerializedName("reviews_count")
    val reviewsCount: Int = 0,
    @SerializedName("feed_count")
    val feedCount: Int = 0,
    @SerializedName("pubs_count")
    val pubsCount: Int = 0,
    @SerializedName("subs_count")
    val subsCount: Int = 0,
    @SerializedName("last_reviews")
    val lastReviews: List<ReviewEntity>? = null,
    @SerializedName("is_registered")
    val isRegistered: Boolean = false,
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("is_subscribed")
    val isSubscribed: Boolean = false,
    @SerializedName("url")
    val url: String? = null,
) : Parcelable
