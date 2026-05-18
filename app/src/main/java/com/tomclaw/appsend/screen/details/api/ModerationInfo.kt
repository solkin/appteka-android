package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

const val MODERATION_STATUS_PENDING = "pending"
const val MODERATION_STATUS_REJECTED = "rejected"

// Moderation status block returned by /app/info for the file owner
// (and moderators). Texts are already translated to the requesting
// client's locale by the service gateway via CachedTranslator.
@GsonModel
@Parcelize
data class ModerationInfo(
    @SerializedName("status")
    val status: String, // "pending", "rejected"
    @SerializedName("reason_code")
    val reasonCode: Int?,
    @SerializedName("reason_text")
    val reasonText: String?,
    @SerializedName("reason_comment")
    val reasonComment: String?,
    @SerializedName("moderator_id")
    val moderatorId: Int?,
    @SerializedName("time")
    val time: Long?,
) : Parcelable
