package com.tomclaw.appsend.screen.installed

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * An update the user started from this screen: once downloaded it installs
 * by itself. [label] and [version] are the ones the file was downloaded
 * under, which is how the download manager finds it again.
 */
@Parcelize
data class PendingInstall(
    val appId: String,
    val label: String,
    val version: String,
) : Parcelable
