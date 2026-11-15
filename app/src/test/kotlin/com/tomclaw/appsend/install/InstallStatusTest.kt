package com.tomclaw.appsend.install

import com.tomclaw.appsend.download.COMPLETED
import com.tomclaw.appsend.download.STARTED
import org.junit.Assert.assertEquals
import org.junit.Test

class InstallStatusTest {

    @Test
    fun `idle install shows the download state through`() {
        assertEquals(COMPLETED, InstallStatus.Idle.asDownloadState(COMPLETED))
    }

    @Test
    fun `writing replaces the finished download with its own percent`() {
        assertEquals(42, InstallStatus.Writing(42).asDownloadState(COMPLETED))
    }

    @Test
    fun `installation by the system has no percent to show`() {
        assertEquals(STARTED, InstallStatus.Installing.asDownloadState(COMPLETED))
    }

    @Test
    fun `waiting for confirmation leaves the completed download available`() {
        assertEquals(COMPLETED, InstallStatus.Confirming.asDownloadState(COMPLETED))
    }

    @Test
    fun `tracker starts idle and keeps the latest status per key`() {
        val tracker = InstallTracker()
        val first = tracker.status("first").test()
        val second = tracker.status("second").test()

        tracker.update("first", InstallStatus.Writing(10))
        tracker.update("first", InstallStatus.Writing(10))
        tracker.update("first", InstallStatus.Installing)

        first.assertValues(
            InstallStatus.Idle,
            InstallStatus.Writing(10),
            InstallStatus.Installing,
        )
        second.assertValue(InstallStatus.Idle)
    }

    @Test
    fun `unknown stored method falls back to the default`() {
        assertEquals(InstallMethodType.DEFAULT, InstallMethodType.fromValue("removed"))
        assertEquals(InstallMethodType.DEFAULT, InstallMethodType.fromValue(null))
        assertEquals(InstallMethodType.SYSTEM, InstallMethodType.fromValue("system"))
    }

}
