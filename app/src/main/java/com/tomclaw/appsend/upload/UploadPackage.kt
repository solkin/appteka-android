package com.tomclaw.appsend.upload

import android.content.pm.PackageInfo
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadPackage(
    val uniqueId: String,
    val packageName: String,
) : Parcelable

@Parcelize
data class UploadApk(
    val path: String,
    val version: String,
    val size: Long,
    val packageInfo: PackageInfo,
) : Parcelable
