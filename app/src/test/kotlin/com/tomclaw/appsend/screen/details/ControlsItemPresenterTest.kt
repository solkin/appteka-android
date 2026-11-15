package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.download.COMPLETED
import com.tomclaw.appsend.install.InstallStatus
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItem
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItemPresenter
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItemView
import com.tomclaw.appsend.util.NOT_INSTALLED
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class ControlsItemPresenterTest {

    @Test
    fun `pending confirmation offers an enabled action and cancellation`() {
        val calls = mutableListOf<String>()
        val view = recorder<ControlsItemView>(calls)
        val presenter = ControlsItemPresenter(recorder<ItemListener>(mutableListOf()))

        presenter.bindView(view, item(InstallStatus.Confirming), 0)

        assertTrue("showContinueButton" in calls)
        assertTrue("showCancelButton" in calls)
        assertFalse("disableInstallButton" in calls)

        calls.clear()
        presenter.bindView(view, item(InstallStatus.Idle), 0)

        assertTrue("showInstallButton" in calls)
        assertFalse("showContinueButton" in calls)
    }

    private fun item(status: InstallStatus) = ControlsItem(
        id = 1,
        appId = "app",
        packageName = "app",
        versionCode = 1,
        sdkVersion = 23,
        androidVersion = "6.0",
        size = 1,
        link = "",
        expiresIn = 60,
        installedVersionCode = NOT_INSTALLED,
        downloadState = COMPLETED,
        installStatus = status,
    )

    private inline fun <reified T : Any> recorder(calls: MutableList<String>): T = requireNotNull(
        T::class.java.cast(
            Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
                calls += method.name
                null
            }
        )
    )
}
