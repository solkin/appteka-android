package com.tomclaw.appsend.delivery

import android.os.Parcelable
import com.tomclaw.appsend.download.DownloadState
import com.tomclaw.appsend.install.InstallStatus
import kotlinx.parcelize.Parcelize

/** Where an app stands on its way from the download to the device, as screens show it. */
sealed interface DeliveryState : Parcelable {

    @Parcelize
    data object Idle : DeliveryState

    /** Waiting for its turn, or for what has to happen before the download can start. */
    @Parcelize
    data object Queued : DeliveryState

    /** [percent] is null until the size is known. */
    @Parcelize
    data class Downloading(val percent: Int?) : DeliveryState

    /** [percent] is null once the system has taken over, as it reports no progress there. */
    @Parcelize
    data class Installing(val percent: Int?) : DeliveryState

    /** Stopped at the system's question, which hasn't reached the user yet. */
    @Parcelize
    data object AwaitingConfirmation : DeliveryState

}

/** Under way: all the user can do is wait for it or cancel it. */
val DeliveryState.isActive: Boolean
    get() = this is DeliveryState.Queued ||
            this is DeliveryState.Downloading ||
            this is DeliveryState.Installing

/** Still on our side, a download or a copy, so our process has to keep running for it. */
val DeliveryState.isRunningHere: Boolean
    get() = this is DeliveryState.Queued ||
            this is DeliveryState.Downloading ||
            this is DeliveryState.Installing && percent != null

/** Percent done of an active delivery, null when there is no figure to show. */
val DeliveryState.progress: Int?
    get() = when (this) {
        is DeliveryState.Downloading -> percent
        is DeliveryState.Installing -> percent
        DeliveryState.Idle, DeliveryState.Queued, DeliveryState.AwaitingConfirmation -> null
    }

internal fun deliveryStateOf(
    download: DownloadState,
    install: InstallStatus,
    installPending: Boolean,
): DeliveryState = when (install) {
    is InstallStatus.Writing -> DeliveryState.Installing(install.percent)
    InstallStatus.Installing -> DeliveryState.Installing(percent = null)
    InstallStatus.AwaitingConfirmation -> DeliveryState.AwaitingConfirmation
    InstallStatus.Idle -> when (download) {
        DownloadState.Queued -> DeliveryState.Queued
        is DownloadState.Running -> DeliveryState.Downloading(download.percent)
        // The copy into the install starts the moment the download completes
        is DownloadState.Completed -> if (installPending) {
            DeliveryState.Installing(percent = 0)
        } else {
            DeliveryState.Idle
        }
        DownloadState.Idle, DownloadState.Failed -> DeliveryState.Idle
    }
}
