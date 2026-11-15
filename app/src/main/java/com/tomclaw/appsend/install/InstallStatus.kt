package com.tomclaw.appsend.install

import android.os.Parcelable
import com.tomclaw.appsend.download.COMPLETED
import com.tomclaw.appsend.download.STARTED
import kotlinx.parcelize.Parcelize

sealed interface InstallStatus : Parcelable {

    @Parcelize
    data object Idle : InstallStatus

    /** Still on us: the APK is being copied into the install session. */
    @Parcelize
    data class Writing(val percent: Int) : InstallStatus

    /** Handed to the system, which gives no finer progress from here on. */
    @Parcelize
    data object Installing : InstallStatus

    /** Waiting for the user to answer the system's confirmation. */
    @Parcelize
    data object Confirming : InstallStatus

}

/**
 * Installing continues what the download started, so a screen drawing download
 * progress can draw this one too: [downloadState] shows through when idle.
 */
fun InstallStatus.asDownloadState(downloadState: Int): Int = when (this) {
    InstallStatus.Idle -> downloadState
    is InstallStatus.Writing -> percent
    InstallStatus.Installing -> STARTED
    InstallStatus.Confirming -> COMPLETED
}
