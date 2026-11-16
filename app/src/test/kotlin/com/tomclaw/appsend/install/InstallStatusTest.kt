package com.tomclaw.appsend.install

import org.junit.Assert.assertEquals
import org.junit.Test

class InstallStatusTest {

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
