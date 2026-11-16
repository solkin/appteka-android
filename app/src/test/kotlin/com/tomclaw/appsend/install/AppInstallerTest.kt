package com.tomclaw.appsend.install

import android.content.Intent
import org.junit.Assert.assertEquals
import org.junit.Test

class AppInstallerTest {

    @Test
    fun `confirmation uses the session while the system installer is preferred`() {
        val session = RecordingMethod()
        val system = RecordingMethod()
        val preferences = object : InstallPreferences {
            override fun method() = InstallMethodType.SYSTEM
            override fun setMethod(method: InstallMethodType) = Unit
        }
        val installer = AppInstallerImpl(preferences, InstallTracker(), session, system)

        installer.confirm("app")

        assertEquals(listOf("app"), session.confirmations)
        assertEquals(emptyList<String>(), system.confirmations)
    }

    private class RecordingMethod : InstallMethod {
        val confirmations = mutableListOf<String>()

        override fun install(request: InstallRequest) = error("Must not start a new install")

        override fun confirm(key: String, confirmation: Intent?) {
            confirmations += key
        }

        override fun cancel(key: String) = error("Must not abandon the session")
    }
}
