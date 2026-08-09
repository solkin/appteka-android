package com.tomclaw.appsend.core.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

/**
 * One content category a viewer can choose not to see, worded by the
 * server in the language of the request.
 *
 * The app holds no vocabulary of its own — no enum, no strings, no
 * knowledge of how many categories exist. [code] is the backend's
 * stable identifier and the only thing sent back; everything on screen
 * comes from [name] and [description]. A reworded category, a new one,
 * or one retired reaches every screen without a release.
 */
@Parcelize
@GsonModel
data class ContentFlag(
    @SerializedName("code")
    val code: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null,
) : Parcelable

@Parcelize
@GsonModel
data class ContentFlagsResponse(
    @SerializedName("flags")
    val flags: List<ContentFlag>,
) : Parcelable
