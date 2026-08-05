package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
data class RejectionReasonsResponse(
    @SerializedName("reasons")
    val reasons: List<RejectionReason>
) : ApiResponse

/**
 * One option from the shared reason catalog. The same catalog explains a
 * moderation rejection, a takedown and a permanent block — which reasons
 * an action may use is the server's call, so the client renders whatever
 * it is handed for the scope it asked about.
 *
 * Text arrives already translated into the requested locale.
 */
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

// Which action a reason may explain. The server keeps the catalog and
// enforces the split; these are the names the client asks by.
const val SCOPE_MODERATION = "moderation"
const val SCOPE_UNPUBLISH = "unpublish"
const val SCOPE_BLOCK = "block"
