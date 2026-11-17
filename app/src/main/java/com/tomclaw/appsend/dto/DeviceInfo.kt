package com.tomclaw.appsend.dto

/**
 * The device builds are picked for: the server reads it from the request
 * headers (AppInfoInterceptor), the app checks ABI compatibility against it.
 */
data class DeviceInfo(
    // Build.SUPPORTED_ABIS, most preferred first.
    val abis: List<String>,
    val sdk: Int,
)
