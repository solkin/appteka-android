package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

const val MODERATION_STATUS_REJECTED = "rejected"
const val MODERATION_STATUS_BLOCKED = "blocked"

// Why the app is not public, returned by /app/info to the author (and
// to moderators). Texts are already translated to the requesting
// client's locale by the service gateway via CachedTranslator.
//
// "rejected" is a takedown the author can still act on; "blocked" is
// permanent. An app the author unpublished themselves carries no block
// at all — nobody imposed anything.
@GsonModel
@Parcelize
data class ModerationInfo(
    @SerializedName("status")
    val status: String, // "rejected", "blocked"
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
