package com.tomclaw.appsend.core

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor that adds application and device information headers to all HTTP requests.
 * This allows the server to personalize responses based on app version, device info,
 * locale, theme, and time.
 */
class AppInfoInterceptor(
    private val appInfoProvider: AppInfoProvider
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(HEADER_APP_PACKAGE, appInfoProvider.getPackageName())
            .header(HEADER_APP_VERSION_NAME, appInfoProvider.getVersionName())
            .header(HEADER_APP_VERSION_CODE, appInfoProvider.getVersionCode().toString())
            .header(HEADER_APP_SIGNATURE, appInfoProvider.getSignature())
            .header(HEADER_ANDROID_VERSION, appInfoProvider.getAndroidVersion().toString())
            .header(HEADER_ANDROID_RELEASE, appInfoProvider.getAndroidRelease())
            .header(HEADER_DEVICE_MANUFACTURER, appInfoProvider.getDeviceManufacturer())
            .header(HEADER_DEVICE_MODEL, appInfoProvider.getDeviceModel())
            .header(HEADER_LOCALE, appInfoProvider.getLocale())
            .header(HEADER_THEME, appInfoProvider.getTheme())
            .header(HEADER_TIMEZONE, appInfoProvider.getTimezone())
            .header(HEADER_LOCAL_TIME, appInfoProvider.getLocalTime())
            .build()

        return chain.proceed(request)
    }

    companion object {
        const val HEADER_APP_PACKAGE = "X-App-Package"
        const val HEADER_APP_VERSION_NAME = "X-App-Version-Name"
        const val HEADER_APP_VERSION_CODE = "X-App-Version-Code"
        const val HEADER_APP_SIGNATURE = "X-App-Signature"
        const val HEADER_ANDROID_VERSION = "X-Android-Version"
        const val HEADER_ANDROID_RELEASE = "X-Android-Release"
        const val HEADER_DEVICE_MANUFACTURER = "X-Device-Manufacturer"
        const val HEADER_DEVICE_MODEL = "X-Device-Model"
        const val HEADER_LOCALE = "X-Locale"
        const val HEADER_THEME = "X-Theme"
        const val HEADER_TIMEZONE = "X-Timezone"
        const val HEADER_LOCAL_TIME = "X-Local-Time"
    }

}
