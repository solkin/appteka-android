package com.tomclaw.appsend.core.permissions

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

/**
 * Decision about a single user-facing action, as returned by the server.
 *
 * Mirrors the Go `capabilities.Capability` struct on the backend. The
 * server is the single source of truth; the client treats every field as
 * read-only input.
 */
@Parcelize
@GsonModel
data class Capability(
    @SerializedName("allowed")
    val allowed: Boolean,
    @SerializedName("reason")
    val reason: String? = null,
    @SerializedName("blocked_by")
    val blockedBy: String? = null,
    @SerializedName("hint_key")
    val hintKey: String? = null,
) : Parcelable

/**
 * Reason codes explaining why an action is denied. Mirror the backend.
 */
object CapabilityReason {
    const val RULE = "rule"
    const val ROLE = "role"
    const val AUTH = "auth"
    const val OWNERSHIP = "ownership"
}
