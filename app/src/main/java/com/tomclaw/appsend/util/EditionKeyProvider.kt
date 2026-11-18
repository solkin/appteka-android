package com.tomclaw.appsend.util

import android.content.pm.PackageManager

/**
 * Edition key (see [editionKey]) of an APK file. Null when the file is not
 * an APK or carries no signatures.
 */
interface EditionKeyProvider {

    fun archiveKey(path: String): String?

}

class EditionKeyProviderImpl(
    private val packageManager: PackageManager,
) : EditionKeyProvider {

    override fun archiveKey(path: String): String? {
        return packageManager.getPackageArchiveInfoCompat(path, SIGNING_INFO_FLAG)?.editionKey()
    }

}
