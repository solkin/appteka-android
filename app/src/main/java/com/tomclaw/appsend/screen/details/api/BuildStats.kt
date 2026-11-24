package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class BuildStats(
    @SerializedName("downloads")
    val downloads: Int,
    @SerializedName("rating")
    val rating: Float,
    @SerializedName("rate_count")
    val rateCount: Int,
    @SerializedName("scores")
    val scores: Scores,
) : Parcelable

const val RATING_SCOPE_EDITION = "edition"
const val RATING_SCOPE_BUILD = "build"
