package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Security(
    @SerializedName("status")
    val status: String,
    @SerializedName("verdict")
    val verdict: String?,
    @SerializedName("score")
    val score: Int?,
) : Parcelable

const val SECURITY_STATUS_PENDING = "pending"
const val SECURITY_STATUS_SCANNING = "scanning"
const val SECURITY_STATUS_COMPLETED = "completed"
const val SECURITY_STATUS_FAILED = "failed"

const val SECURITY_VERDICT_SAFE = "safe"
const val SECURITY_VERDICT_SUSPICIOUS = "suspicious"
const val SECURITY_VERDICT_MALWARE = "malware"
const val SECURITY_VERDICT_UNKNOWN = "unknown"

