package com.tomclaw.appsend.screen.gallery

import android.content.Context
import android.net.Uri
import java.io.OutputStream

interface StreamsProvider {

    fun openOutputStream(uri: Uri): OutputStream?

}

class StreamsProviderImpl(private val context: Context) : StreamsProvider {

    override fun openOutputStream(uri: Uri): OutputStream? {
        return context.contentResolver.openOutputStream(uri)
    }

}
