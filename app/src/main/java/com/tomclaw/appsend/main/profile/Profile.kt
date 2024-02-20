package com.tomclaw.appsend.main.profile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class Profile(
    @SerializedName("user_id")
    val userId: Int = 0,
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
    @SerializedName("total_downloads")
    val totalDownloads: Int = 0,
    @SerializedName("msg_count")
    val msgCount: Int = 0,
    @SerializedName("ratings_count")
    val ratingsCount: Int = 0,
    @SerializedName("mentor_of_count")
    val moderatorsCount: Int = 0,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("is_registered")
    val isRegistered: Boolean = false,
    @SerializedName("is_verified")
    val isVerified: Boolean = false,
    @SerializedName("url")
    val url: String? = null,
) : Parcelable
