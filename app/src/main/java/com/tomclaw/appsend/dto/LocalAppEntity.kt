package com.tomclaw.appsend.dto

import android.content.pm.PackageInfo
import android.os.Parcelable
import com.tomclaw.appsend.util.FileHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class LocalAppEntity(
    val label: String,
    val packageName: String,
    val version: String,
    val path: String,
    val size: Long,
    val packageInfo: PackageInfo,
) : Parcelable

fun LocalAppEntity.getApkPrefix(): String {
    return FileHelper.escapeFileSymbols("$label-$version")
}

fun LocalAppEntity.getApkName(): String {
    return getApkPrefix() + APK_EXTENSION
}

fun LocalAppEntity.getIconName(): String {
    return getApkPrefix() + ICON_EXTENSION
}

const val APK_EXTENSION = ".apk"
const val ICON_EXTENSION = ".png"
