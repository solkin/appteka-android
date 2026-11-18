package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.UserMark
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

/**
 * Builds of a package under one signing key: they install over each other,
 * a build of another edition does not. Builds whose key is unknown come as
 * one group with no [key] and no [ref].
 */
@GsonModel
@Parcelize
data class AppEdition(
    @SerializedName("key")
    val key: String?,
    @SerializedName("ref")
    val ref: String?,
    // The edition of the build on screen.
    @SerializedName("current")
    val current: Boolean?,
    // Uploader of the edition's latest publication.
    @SerializedName("publisher")
    val publisher: UserMark?,
    @SerializedName("official")
    val official: Boolean?,
    @SerializedName("origin")
    val origin: String?,
    @SerializedName("downloads")
    val downloads: Int?,
    // Highest version first.
    @SerializedName("versions")
    val versions: List<AppVersion>?,
) : Parcelable
