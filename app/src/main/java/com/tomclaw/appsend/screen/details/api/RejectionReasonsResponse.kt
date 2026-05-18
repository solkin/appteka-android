package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.ApiResponse
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
data class RejectionReasonsResponse(
    @SerializedName("reasons")
    val reasons: List<RejectionReason>
) : ApiResponse

@GsonModel
@Parcelize
data class RejectionReason(
    @SerializedName("code")
    val code: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("requires_comment")
    val requiresComment: Boolean,
) : Parcelable
