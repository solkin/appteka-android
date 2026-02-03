package com.tomclaw.appsend.user

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class SessionCredentials(
    @SerializedName("guid")
    val guid: String
) : Parcelable
