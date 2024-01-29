package com.tomclaw.appsend.core

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class DeviceIdInterceptor(private val deviceId: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("X-Device-ID", deviceId)
                .build()
        )
    }

}
