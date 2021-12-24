package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserData(
    @SerializedName("guid")
    val guid: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_icon")
    val userIcon: UserIcon,
    @SerializedName("role")
    val role: Int,
    @SerializedName("email")
    val email: String?,
    @SerializedName("name")
    val name: String?,
) : Parcelable
