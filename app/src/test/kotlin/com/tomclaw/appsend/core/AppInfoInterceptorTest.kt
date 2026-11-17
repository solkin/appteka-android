package com.tomclaw.appsend.core

import com.tomclaw.appsend.dto.DeviceInfo
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class AppInfoInterceptorTest {

    // The server picks builds by these two headers; their names and format
    // are its contract.
    @Test
    fun `every request carries the device ABIs in preference order and its SDK`() {
        val device = DeviceInfo(abis = listOf("arm64-v8a", "armeabi-v7a", "armeabi"), sdk = 34)
        var sent: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(AppInfoInterceptor(FakeAppInfoProvider(), device))
            .addInterceptor { chain ->
                sent = chain.request()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("".toResponseBody())
                    .build()
            }
            .build()

        client.newCall(Request.Builder().url("https://appteka.store/api/1/app/info").build()).execute().close()

        assertEquals("arm64-v8a,armeabi-v7a,armeabi", sent?.header("X-Device-ABIs"))
        assertEquals("34", sent?.header("X-Android-Version"))
    }

    private class FakeAppInfoProvider : AppInfoProvider {
        override fun getPackageName() = "com.tomclaw.appsend"
        override fun getVersionName() = "24.0"
        override fun getVersionCode() = 1L
        override fun getSignature() = "sig"
        override fun getAndroidRelease() = "14"
        override fun getDeviceManufacturer() = "Google"
        override fun getDeviceModel() = "Pixel"
        override fun getLocale() = "en"
        override fun getTheme() = "light"
        override fun getTimezone() = "UTC"
        override fun getLocalTime() = "2026-11-18T00:00:00+00:00"
    }

}
