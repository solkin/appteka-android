package com.tomclaw.appsend.screen.installed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InstalledAppEntity(
    val packageName: String,
    val label: String,
    val icon: String?,
    val verName: String,
    val verCode: Long,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val size: Long,
    val path: String?,
    val isUserApp: Boolean,
) : Parcelable
