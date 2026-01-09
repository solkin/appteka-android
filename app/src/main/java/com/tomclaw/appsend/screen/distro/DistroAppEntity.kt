package com.tomclaw.appsend.screen.distro

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DistroAppEntity(
    val packageName: String,
    val label: String,
    val icon: String?,
    val verName: String,
    val verCode: Long,
    val lastModified: Long,
    val size: Long,
    val path: String?,
    val fileName: String,
) : Parcelable
