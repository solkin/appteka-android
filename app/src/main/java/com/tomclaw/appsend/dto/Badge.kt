package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

/**
 * Compact badge representation that travels alongside any user
 * snapshot (avatar, profile, review). The icon is a raw inline SVG
 * already localized for the active locale by the backend, so the
 * client just renders it as-is on a colored circle attached to the
 * avatar. There is at most one [BadgeMark] per user — the highest
 * priority one in their personal order.
 */
@Parcelize
@GsonModel
data class BadgeMark(
    @SerializedName("code")
    val code: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("color")
    val color: String?,
    @SerializedName("name")
    val name: String,
) : Parcelable

/**
 * Full badge entry shown as a chip on the profile screen, with a
 * human-readable description used in the bottom-sheet preview.
 */
@Parcelize
@GsonModel
data class Badge(
    @SerializedName("code")
    val code: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("color")
    val color: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
) : Parcelable
