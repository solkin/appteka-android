package com.tomclaw.appsend.user.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserDataResponse(
    @SerializedName("profile")
    val profile: UserProfile
) : Parcelable
