package com.tomclaw.appsend.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * Flag that makes [PackageInfo] carry what [editionKey] reads.
 */
val SIGNING_INFO_FLAG: Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        PackageManager.GET_SIGNING_CERTIFICATES
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_SIGNATURES
    }

/**
 * Edition key of an installed package, computed as the server does
 * (common/apksign): an update installs over this package only if it has
 * the same key. Null when the package info carries no signatures.
 */
fun PackageInfo.editionKey(): String? {
    val certs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val info = signingInfo ?: return null
        if (info.hasMultipleSigners()) {
            info.apkContentsSigners?.toList()
        } else {
            // History runs from the original certificate to the current one,
            // and the original is what survives a key rotation.
            listOfNotNull(info.signingCertificateHistory?.firstOrNull())
        }
    } else {
        @Suppress("DEPRECATION")
        signatures?.toList()
    }
    return editionKeyOf(certs.orEmpty().map { it.toByteArray() })
}

/**
 * Key over DER-encoded signer certificates: the hash of the only one, or
 * the hash of the sorted hashes of several.
 */
fun editionKeyOf(signerCerts: List<ByteArray>): String? {
    val hashes = signerCerts.map { sha256Hex(it) }
    return when (hashes.size) {
        0 -> null
        1 -> hashes.first()
        else -> sha256Hex(hashes.sorted().joinToString("").toByteArray())
    }
}

private fun sha256Hex(bytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
    val result = StringBuilder(digest.size * 2)
    digest.forEach {
        val i = it.toInt()
        result.append(LOWER_HEX_CHARS[i shr 4 and 0x0f])
        result.append(LOWER_HEX_CHARS[i and 0x0f])
    }
    return result.toString()
}

private const val LOWER_HEX_CHARS = "0123456789abcdef"
