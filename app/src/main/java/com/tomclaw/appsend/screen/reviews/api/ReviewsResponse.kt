package com.tomclaw.appsend.screen.reviews.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewsResponse(
    @SerializedName("entries")
    val entries: List<ReviewEntity>,
) : Parcelable
