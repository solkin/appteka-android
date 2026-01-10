package com.tomclaw.bananalytics

/**
 * Configuration for Bananalytics SDK.
 *
 * @param baseUrl Base URL for the analytics server (e.g., "https://banana.appteka.store")
 * @param apiKey API key for authentication (format: "bnn_xxxxx")
 */
data class BananalyticsConfig(
    val baseUrl: String,
    val apiKey: String,
)
