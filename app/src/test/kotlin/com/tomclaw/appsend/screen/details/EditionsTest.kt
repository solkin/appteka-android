package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.dto.UserMark
import com.tomclaw.appsend.screen.details.api.AppEdition
import com.tomclaw.appsend.screen.details.api.AppVersion
import com.tomclaw.appsend.util.NOT_INSTALLED
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditionsTest {

    @Test
    fun `install state compares versions inside one edition`() {
        assertEquals(InstallState.NOT_INSTALLED, installState(InstalledApp.NONE, 10, KEY_A))
        assertEquals(InstallState.OLDER, installState(InstalledApp(9, KEY_A, removable = true), 10, KEY_A))
        assertEquals(InstallState.SAME, installState(InstalledApp(10, KEY_A, removable = true), 10, KEY_A))
        assertEquals(InstallState.NEWER, installState(InstalledApp(11, KEY_A, removable = true), 10, KEY_A))
    }

    @Test
    fun `another key is never an update, whatever the versions`() {
        assertEquals(InstallState.OTHER_EDITION, installState(InstalledApp(9, KEY_B, removable = true), 10, KEY_A))
        assertEquals(InstallState.OTHER_EDITION, installState(InstalledApp(11, KEY_B, removable = true), 10, KEY_A))
    }

    @Test
    fun `an unknown key on either side leaves it to versions`() {
        assertEquals(InstallState.OLDER, installState(InstalledApp(9, null, removable = true), 10, KEY_A))
        assertEquals(InstallState.OLDER, installState(InstalledApp(9, KEY_B, removable = true), 10, null))
    }

    @Test
    fun `the build on screen takes its key from the current edition`() {
        val details = testDetails(versionCode = 10, editions = listOf(edition(KEY_A, current = true, 10)))
        assertEquals(InstallState.OTHER_EDITION, details.installState(InstalledApp(9, KEY_B, removable = true)))
        assertEquals(InstallState.OLDER, testDetails(versionCode = 10, editions = null).installState(InstalledApp(9, KEY_B, removable = true)))
    }

    @Test
    fun `newer marks only the installed edition, above the installed version`() {
        val details = testDetails(
            versionCode = 30,
            editions = listOf(
                edition(KEY_B, current = true, 30, 20),
                edition(KEY_A, current = false, 12, 11, 9),
                edition(null, current = false, 99),
            ),
        )
        val groups = details.versionsByEdition(InstalledApp(10, KEY_A, removable = true))

        assertEquals(listOf(KEY_B, KEY_A, null), groups.map { it.edition?.key })
        assertEquals(listOf(false, true, false), groups.map { it.installed })
        assertEquals(listOf(false, false), groups[0].versions.map { it.newer })
        assertEquals(listOf(true, true, false), groups[1].versions.map { it.newer })
        assertEquals(listOf(false), groups[2].versions.map { it.newer })
    }

    @Test
    fun `without a known installed key, newer is said against the build on screen`() {
        val details = testDetails(
            versionCode = 20,
            editions = listOf(edition(KEY_A, current = true, 30, 20), edition(KEY_B, current = false, 40)),
        )
        val notInstalled = details.versionsByEdition(InstalledApp.NONE)
        assertEquals(listOf(true, false), notInstalled[0].versions.map { it.newer })
        assertEquals(listOf(false), notInstalled[1].versions.map { it.newer })

        val unreadable = details.versionsByEdition(InstalledApp(20, null, removable = true))
        assertEquals(listOf(true, false), unreadable[0].versions.map { it.newer })
        assertFalse(unreadable[0].installed)
    }

    @Test
    fun `an installed key the store does not have marks nothing`() {
        val details = testDetails(versionCode = 20, editions = listOf(edition(KEY_A, current = true, 30)))
        val groups = details.versionsByEdition(InstalledApp(1, KEY_B, removable = true))
        assertFalse(groups.single().installed)
        assertFalse(groups.single().versions.single().newer)
    }

    @Test
    fun `a server without editions gives one flat list, highest first`() {
        val details = testDetails(versionCode = 20, editions = null, flat = listOf(version(10), version(30), version(20)))
        val group = details.versionsByEdition(InstalledApp(5, KEY_A, removable = true)).single()
        assertNull(group.edition)
        assertEquals(listOf(30, 20, 10), group.versions.map { it.version.verCode })
        assertEquals(listOf(true, false, false), group.versions.map { it.newer })
    }

    @Test
    fun `own edition is found by the full key`() {
        val own = edition(KEY_A, current = false, 1)
        val details = testDetails(versionCode = 1, editions = listOf(edition(KEY_B, current = true, 1), own))
        assertEquals(own, details.editionOf(KEY_A))
        assertNull(details.editionOf(KEY_A.take(24)))
        assertNull(details.editionOf(null))
        assertTrue(details.currentEdition()?.key == KEY_B)
    }

    @Test
    fun `not installed is not installed`() {
        assertFalse(InstalledApp(NOT_INSTALLED, KEY_A, removable = true).isInstalled)
    }

    private fun edition(key: String?, current: Boolean, vararg codes: Int) = AppEdition(
        key = key,
        ref = key?.let { "app/${it.take(24)}" },
        current = current,
        publisher = UserMark(id = 1),
        official = null,
        origin = null,
        downloads = 0,
        versions = codes.map { version(it) },
    )

    private fun version(code: Int) = AppVersion(
        appId = "id$code",
        downloads = 0,
        verCode = code,
        verName = "$code",
        sdkVersion = 21,
    )

}

private const val KEY_A = "aaaaaaaaaaaaaaaaaaaaaaaa0000000000000000000000000000000000000000"
private const val KEY_B = "bbbbbbbbbbbbbbbbbbbbbbbb0000000000000000000000000000000000000000"
