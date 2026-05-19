package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.ApiResponse
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
data class AIReviewResponse(
    @SerializedName("review")
    val review: AIReview?
) : ApiResponse

@GsonModel
@Parcelize
data class AIReview(
    @SerializedName("file_id") val fileId: Int,
    @SerializedName("decision") val decision: Int, // 1=approve, -1=reject, 0=uncertain
    @SerializedName("confidence") val confidence: Float,
    @SerializedName("reason_code") val reasonCode: Int?,
    @SerializedName("reason_text") val reasonText: String?,
    @SerializedName("provider") val provider: String,
    @SerializedName("model") val model: String,
    @SerializedName("created_at") val createdAt: Long,
) : Parcelable
