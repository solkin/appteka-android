package com.tomclaw.appsend.core

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.tomclaw.appsend.util.isDarkTheme
import com.tomclaw.appsend.util.sha256
import com.tomclaw.appsend.util.versionCodeCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

interface AppInfoProvider {

    fun getPackageName(): String

    fun getVersionName(): String

    fun getVersionCode(): Long

    fun getSignature(): String

    fun getAndroidVersion(): Int

    fun getAndroidRelease(): String

    fun getDeviceManufacturer(): String

    fun getDeviceModel(): String

    fun getLocale(): String

    fun getTheme(): String

    fun getTimezone(): String

    fun getLocalTime(): String

}

class AppInfoProviderImpl(
    private val context: Context,
    private val packageManager: PackageManager,
    private val locale: Locale,
) : AppInfoProvider {

    private val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    override fun getPackageName(): String = packageInfo.packageName

    override fun getVersionName(): String = packageInfo.versionName.orEmpty()

    override fun getVersionCode(): Long = packageInfo.versionCodeCompat()

    override fun getSignature(): String {
        return try {
            val signatures = getPackageSignatures(packageInfo.packageName)
            if (signatures.isNotEmpty()) {
                signatures.first().toByteArray().toHexString().sha256()
            } else {
                UNKNOWN
            }
        } catch (e: Exception) {
            UNKNOWN
        }
    }

    override fun getAndroidVersion(): Int = Build.VERSION.SDK_INT

    override fun getAndroidRelease(): String = Build.VERSION.RELEASE

    override fun getDeviceManufacturer(): String = Build.MANUFACTURER

    override fun getDeviceModel(): String = Build.MODEL

    override fun getLocale(): String = "${locale.language}-${locale.country}"

    override fun getTheme(): String = if (context.isDarkTheme()) "dark" else "light"

    override fun getTimezone(): String = TimeZone.getDefault().id

    override fun getLocalTime(): String {
        val now = Date()
        val formatted = dateFormat.format(now)
        val tz = TimeZone.getDefault()
        val offsetMs = tz.getOffset(now.time)
        val offsetHours = offsetMs / 3600000
        val offsetMinutes = Math.abs((offsetMs % 3600000) / 60000)
        val sign = if (offsetHours >= 0) "+" else "-"
        return String.format(Locale.US, "%s%s%02d:%02d", formatted, sign, Math.abs(offsetHours), offsetMinutes)
    }

    private fun getPackageSignatures(packageName: String): Array<Signature> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            info.signingInfo?.apkContentsSigners ?: emptyArray()
        } else {
            @Suppress("DEPRECATION")
            val info = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            )
            @Suppress("DEPRECATION")
            info.signatures ?: emptyArray()
        }
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val UNKNOWN = "unknown"
    }

}
