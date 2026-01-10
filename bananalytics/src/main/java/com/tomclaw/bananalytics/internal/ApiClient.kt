package com.tomclaw.bananalytics.internal

import com.google.gson.Gson
import com.tomclaw.bananalytics.BananalyticsConfig
import com.tomclaw.bananalytics.api.SubmitCrashesRequest
import com.tomclaw.bananalytics.api.SubmitEventsRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Internal HTTP client for sending analytics data to the backend.
 */
internal class ApiClient(
    private val config: BananalyticsConfig,
    private val gson: Gson,
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Sends analytics events to the backend.
     * @return true if events were sent successfully, false otherwise
     */
    fun sendEvents(request: SubmitEventsRequest): Boolean {
        return post(EVENTS_ENDPOINT, request)
    }

    /**
     * Sends crash reports to the backend.
     * @return true if crashes were sent successfully, false otherwise
     */
    fun sendCrashes(request: SubmitCrashesRequest): Boolean {
        return post(CRASHES_ENDPOINT, request)
    }

    private fun <T> post(endpoint: String, body: T): Boolean {
        val url = "${config.baseUrl.trimEnd('/')}/$endpoint"
        val json = gson.toJson(body)
        val requestBody = json.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .header(HEADER_API_KEY, config.apiKey)
            .header(HEADER_CONTENT_TYPE, "application/json")
            .post(requestBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: IOException) {
            false
        }
    }

    companion object {
        private const val EVENTS_ENDPOINT = "api/v1/events/submit"
        private const val CRASHES_ENDPOINT = "api/v1/crashes/submit"

        private const val HEADER_API_KEY = "X-API-Key"
        private const val HEADER_CONTENT_TYPE = "Content-Type"

        private const val CONNECT_TIMEOUT_SECONDS = 30L
        private const val READ_TIMEOUT_SECONDS = 30L
        private const val WRITE_TIMEOUT_SECONDS = 30L
    }
}
