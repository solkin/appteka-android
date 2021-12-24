package com.tomclaw.appsend.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SessionCredentials(
    @SerializedName("guid")
    val guid: String
) : Parcelable
