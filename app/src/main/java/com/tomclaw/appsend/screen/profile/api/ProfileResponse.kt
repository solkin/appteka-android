package com.tomclaw.appsend.screen.profile.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileResponse(
    @SerializedName("profile")
    val profile: Profile,
    @SerializedName("grant_roles")
    val grantRoles: List<Int>?,
) : Parcelable
