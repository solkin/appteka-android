package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
data class UserIcon(
    @SerializedName("icon")
    val icon: String,
    /**
     * Per-locale display label. Nullable defensively: the wire
     * format is `map[string]string` and a server-side regression
     * (or a partially populated zero-value DTO) can ship `null` for
     * the field. Treating it as `Map?` keeps Parcelable serialization
     * crash-free; consumers fall back via `label?.get(...)`.
     */
    @SerializedName("label")
    val label: Map<String, String>? = null,
    @SerializedName("color")
    val color: String,
) : Parcelable
