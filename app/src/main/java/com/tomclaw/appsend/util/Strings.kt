package com.tomclaw.appsend.util

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.CRC32

fun String.sha512() = hashString(type = "SHA-512", input = this)

fun String.sha256() = hashString(type = "SHA-256", input = this)

fun String.sha1() = hashString(type = "SHA-1", input = this)

fun String.md5() = hashString(type = "MD5", input = this)

/**
 * Supported algorithms on Android:
 *
 * Algorithm	Supported API Levels
 * MD5          1+
 * SHA-1	    1+
 * SHA-224	    1-8,22+
 * SHA-256	    1+
 * SHA-384	    1+
 * SHA-512	    1+
 */
private fun hashString(type: String, input: String): String {
    val bytes = MessageDigest
        .getInstance(type)
        .digest(input.toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }

    return result.toString()
}

fun String.crc32(): Int {
    val crc32Calculator = CRC32()
    crc32Calculator.update(this.toByteArray())
    return crc32Calculator.value.toInt()
}

fun String.capitalize(locale: Locale): String {
    return replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}

fun String.formatQuote(): SpannableStringBuilder {
    val string = this
    val spannable = SpannableStringBuilder(string)
    val quoteStart = string.indexOf('>')
    val quoteEnd = string.indexOf('\n', quoteStart)
    if (quoteStart >= 0 && quoteEnd > 0) {
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            quoteStart,
            quoteEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannable
}

const val HEX_CHARS = "0123456789ABCDEF"
