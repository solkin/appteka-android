package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

const val DEV_REQUEST_STATUS_NEW = "new"
const val DEV_REQUEST_STATUS_APPROVED = "approved"
const val DEV_REQUEST_STATUS_REJECTED = "rejected"

// The uploader's application for developer status, filed by declaring a
// build their own. Sent to the uploader only; the copy lives in the app,
// the server gives just the state and, on a refusal, the reason.
@GsonModel
@Parcelize
data class DevRequest(
    @SerializedName("status")
    val status: String?,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("time")
    val time: Long?,
) : Parcelable
