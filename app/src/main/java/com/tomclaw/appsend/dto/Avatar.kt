package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

/**
 * User-uploaded avatar image, embedded into [UserIcon] as an
 * optional facet. Clients prefer the bitmap when present and fall
 * back to the procedural SVG glyph + colour otherwise. Both URLs
 * are pre-signed CDN links produced by the server.
 */
@Parcelize
@GsonModel
data class Avatar(
    @SerializedName("preview_url")
    val previewUrl: String,
    @SerializedName("original_url")
    val originalUrl: String,
) : Parcelable
