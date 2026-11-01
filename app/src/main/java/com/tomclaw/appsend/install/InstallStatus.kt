package com.tomclaw.appsend.install

import com.tomclaw.appsend.download.STARTED

sealed interface InstallStatus {

    data object Idle : InstallStatus

    /** Still on us: the APK is being copied into the install session. */
    data class Writing(val percent: Int) : InstallStatus

    /** Handed to the system, which gives no finer progress from here on. */
    data object Installing : InstallStatus

    /** Waiting for the user to answer the system's confirmation. */
    data object Confirming : InstallStatus

}

/**
 * Installing continues what the download started, so a screen drawing download
 * progress can draw this one too: [downloadState] shows through when idle.
 */
fun InstallStatus.asDownloadState(downloadState: Int): Int = when (this) {
    InstallStatus.Idle -> downloadState
    is InstallStatus.Writing -> percent
    InstallStatus.Installing, InstallStatus.Confirming -> STARTED
}
