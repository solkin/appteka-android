package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

/**
 * A language the app is translated into, named by the server in the
 * language of the request. The app keeps no list of language names, so
 * [name] is what goes on screen; [code] is the stable identifier.
 */
@GsonModel
@Parcelize
data class AppLanguage(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
) : Parcelable
