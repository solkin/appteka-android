package com.tomclaw.appsend.screen.reviews.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
data class ReviewsResponse(
    @SerializedName("entries")
    val entries: List<ReviewEntity>,
) : Parcelable
