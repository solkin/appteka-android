package com.tomclaw.appsend.dto

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

/**
 * What the server needs to offer only builds that install on this device.
 */
@GsonModel
data class DeviceInfo(
    // Build.SUPPORTED_ABIS, most preferred first.
    @SerializedName("abis")
    val abis: List<String>,
    @SerializedName("sdk")
    val sdk: Int,
)
