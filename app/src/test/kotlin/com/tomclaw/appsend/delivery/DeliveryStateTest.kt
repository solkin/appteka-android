package com.tomclaw.appsend.delivery

import android.net.TestUri
import com.tomclaw.appsend.download.DownloadState
import com.tomclaw.appsend.install.InstallStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class DeliveryStateTest {

    private val completed = DownloadState.Completed(TestUri())

    @Test
    fun `download stages show while nothing is installing`() {
        assertEquals(DeliveryState.Idle, deliveryStateOf(DownloadState.Idle, InstallStatus.Idle, false))
        assertEquals(DeliveryState.Queued, deliveryStateOf(DownloadState.Queued, InstallStatus.Idle, true))
        assertEquals(
            DeliveryState.Downloading(percent = null),
            deliveryStateOf(DownloadState.Running(percent = null), InstallStatus.Idle, true),
        )
        assertEquals(
            DeliveryState.Downloading(42),
            deliveryStateOf(DownloadState.Running(42), InstallStatus.Idle, true),
        )
        assertEquals(DeliveryState.Idle, deliveryStateOf(DownloadState.Failed, InstallStatus.Idle, false))
    }

    @Test
    fun `a completed download is installing until it is handed over, and idle afterwards`() {
        assertEquals(DeliveryState.Installing(0), deliveryStateOf(completed, InstallStatus.Idle, true))
        assertEquals(DeliveryState.Idle, deliveryStateOf(completed, InstallStatus.Idle, false))
    }

    @Test
    fun `install stages take over the download`() {
        assertEquals(DeliveryState.Installing(30), deliveryStateOf(completed, InstallStatus.Writing(30), false))
        assertEquals(
            DeliveryState.Installing(percent = null),
            deliveryStateOf(DownloadState.Idle, InstallStatus.Installing, false),
        )
        assertEquals(
            DeliveryState.AwaitingConfirmation,
            deliveryStateOf(completed, InstallStatus.AwaitingConfirmation, false),
        )
    }

    @Test
    fun `only an unseen question stops the delivery for the user`() {
        assertEquals(false, DeliveryState.AwaitingConfirmation.isActive)
        assertEquals(false, DeliveryState.Idle.isActive)
        assertEquals(true, DeliveryState.Queued.isActive)
        assertEquals(true, DeliveryState.Downloading(1).isActive)
        assertEquals(true, DeliveryState.Installing(percent = null).isActive)
        assertEquals(1, DeliveryState.Downloading(1).progress)
        assertEquals(null, DeliveryState.Queued.progress)
    }

    @Test
    fun `our process is needed until the system has the install`() {
        assertEquals(true, DeliveryState.Queued.isRunningHere)
        assertEquals(true, DeliveryState.Downloading(percent = null).isRunningHere)
        assertEquals(true, DeliveryState.Installing(0).isRunningHere)
        assertEquals(false, DeliveryState.Installing(percent = null).isRunningHere)
        assertEquals(false, DeliveryState.AwaitingConfirmation.isRunningHere)
        assertEquals(false, DeliveryState.Idle.isRunningHere)
    }

}
