package com.tomclaw.appsend.user.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class UserBrief(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_icon")
    val userIcon: UserIcon,
    @SerializedName("join_time")
    val joinTime: Long,
    @SerializedName("last_seen")
    val lastSeen: Long,
    @SerializedName("role")
    val role: Int,
    @SerializedName("mentor_id")
    val mentorId: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("is_registered")
    val isRegistered: Boolean,
    @SerializedName("is_verified")
    val isVerified: Boolean,
    @SerializedName("url")
    val url: String?,
) : Parcelable
