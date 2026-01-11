package com.tomclaw.appsend.util

import android.content.res.Resources
import com.tomclaw.appsend.R

object FileHelper {

    private val RESERVED_CHARS = arrayOf("|", "\\", "/", "?", "*", "<", "\"", ":", ">")

    fun formatBytes(resources: Resources, bytes: Long): String {
        return when {
            bytes < 1024 -> resources.getString(R.string.bytes, bytes)
            bytes < 1024 * 1024 -> resources.getString(R.string.kibibytes, bytes / 1024.0f)
            bytes < 1024 * 1024 * 1024 -> resources.getString(R.string.mibibytes, bytes / 1024.0f / 1024.0f)
            else -> resources.getString(R.string.gigibytes, bytes / 1024.0f / 1024.0f / 1024.0f)
        }
    }

    fun escapeFileSymbols(name: String): String {
        var result = name
        for (symbol in RESERVED_CHARS) {
            result = result.replace(symbol[0], '_')
        }
        return result
    }

}
