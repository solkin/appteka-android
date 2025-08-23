package com.tomclaw.appsend.core

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream
import java.io.OutputStream

interface StreamsProvider {

    fun openInputStream(uri: Uri): InputStream?

    fun openOutputStream(uri: Uri): OutputStream?

}

class StreamsProviderImpl(
    private val context: Context,
    private val client: OkHttpClient,
) : StreamsProvider {

    @SuppressLint("Recycle")
    override fun openInputStream(uri: Uri): InputStream? {
        when(uri.scheme) {
            ContentResolver.SCHEME_CONTENT,
            ContentResolver.SCHEME_FILE,
            ContentResolver.SCHEME_ANDROID_RESOURCE -> return context.contentResolver.openInputStream(uri)
            SCHEME_HTTP,
            SCHEME_HTTPS -> {
                val request: Request = Request.Builder().url(uri.toString()).build()
                client.newCall(request).execute().let { response ->
                    return response.body.byteStream()
                }
            }
        }
        return null
    }

    override fun openOutputStream(uri: Uri): OutputStream? {
        return context.contentResolver.openOutputStream(uri)
    }

}

private const val SCHEME_HTTP = "http"
private const val SCHEME_HTTPS = "https"
