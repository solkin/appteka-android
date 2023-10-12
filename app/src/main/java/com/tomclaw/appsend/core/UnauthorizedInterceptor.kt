package com.tomclaw.appsend.core

import android.content.Context
import android.content.Intent
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import okhttp3.Interceptor
import okhttp3.Response


class UnauthorizedInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code() == 401) {
            handleUnauthorizedResponse()
        }
        return response
    }

    private fun handleUnauthorizedResponse() {
        val intent = createRequestCodeActivityIntent(context).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        context.startActivity(intent)
    }

}
