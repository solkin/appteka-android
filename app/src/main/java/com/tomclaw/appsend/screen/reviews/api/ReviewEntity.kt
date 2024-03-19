package com.tomclaw.appsend.screen.reviews.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.details.api.RatingEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewEntity(
    @SerializedName("file")
    val file: AppEntity,
    @SerializedName("rating")
    val rating: RatingEntity,
) : Parcelable
