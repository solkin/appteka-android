package com.tomclaw.appsend.screen.details.adapter.play

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.FileHelper

interface PlayResourceProvider {

    fun formatFileSize(size: Long): String

    fun securityScanningShort(): String

    fun securitySafeShort(): String

    fun securitySuspiciousShort(): String

    fun securityMalwareShort(): String

    fun securityNotCheckedShort(): String

}

class PlayResourceProviderImpl(val resources: Resources) : PlayResourceProvider {

    override fun formatFileSize(size: Long): String {
        return FileHelper.formatBytes(resources, size)
    }

    override fun securityScanningShort(): String {
        return resources.getString(R.string.security_scanning_short)
    }

    override fun securitySafeShort(): String {
        return resources.getString(R.string.security_safe_short)
    }

    override fun securitySuspiciousShort(): String {
        return resources.getString(R.string.security_suspicious_short)
    }

    override fun securityMalwareShort(): String {
        return resources.getString(R.string.security_malware_short)
    }

    override fun securityNotCheckedShort(): String {
        return resources.getString(R.string.security_not_checked_short)
    }

}