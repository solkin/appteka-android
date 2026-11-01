package com.tomclaw.appsend.install

import android.content.pm.PackageInstaller
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InstallFailureTest {

    @Test
    fun `a downgrade is not mistaken for a broken file`() {
        assertEquals(
            InstallFailure.DOWNGRADE,
            InstallFailure.from(
                PackageInstaller.STATUS_FAILURE_INVALID,
                "INSTALL_FAILED_VERSION_DOWNGRADE: Downgrade detected: Update version code 1 is older than current 2"
            )
        )
    }

    @Test
    fun `falling back to an older permission model counts as a downgrade`() {
        assertEquals(
            InstallFailure.DOWNGRADE,
            InstallFailure.from(
                PackageInstaller.STATUS_FAILURE_INVALID,
                "INSTALL_FAILED_PERMISSION_MODEL_DOWNGRADE: Package targets runtime permissions"
            )
        )
    }

    @Test
    fun `another signature is named as such`() {
        assertEquals(
            InstallFailure.SIGNATURE,
            InstallFailure.from(
                PackageInstaller.STATUS_FAILURE_CONFLICT,
                "INSTALL_FAILED_UPDATE_INCOMPATIBLE: Existing package signatures do not match"
            )
        )
    }

    @Test
    fun `incompatibility says what exactly does not fit`() {
        val status = PackageInstaller.STATUS_FAILURE_INCOMPATIBLE
        assertEquals(InstallFailure.ABI, InstallFailure.from(status, "INSTALL_FAILED_NO_MATCHING_ABIS: x"))
        assertEquals(InstallFailure.ANDROID_TOO_OLD, InstallFailure.from(status, "INSTALL_FAILED_OLDER_SDK: x"))
        assertEquals(InstallFailure.APP_TOO_OLD, InstallFailure.from(status, "INSTALL_FAILED_DEPRECATED_SDK_VERSION: x"))
    }

    @Test
    fun `a restriction is not called an incompatibility`() {
        assertEquals(
            InstallFailure.BLOCKED,
            InstallFailure.from(PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, "INSTALL_FAILED_USER_RESTRICTED: x")
        )
    }

    @Test
    fun `the user's own refusal needs no message`() {
        assertNull(
            InstallFailure.from(PackageInstaller.STATUS_FAILURE_ABORTED, "INSTALL_FAILED_ABORTED: User rejected permissions")
        )
        assertNull(InstallFailure.from(PackageInstaller.STATUS_FAILURE_ABORTED, null))
    }

    @Test
    fun `a verifier's refusal is not taken for the user's`() {
        assertEquals(
            InstallFailure.BLOCKED,
            InstallFailure.from(PackageInstaller.STATUS_FAILURE_ABORTED, "INSTALL_FAILED_VERIFICATION_FAILURE: x")
        )
    }

    @Test
    fun `the status alone still gives a true answer when the message format changes`() {
        assertEquals(InstallFailure.CONFLICT, InstallFailure.from(PackageInstaller.STATUS_FAILURE_CONFLICT, "new format"))
        assertEquals(InstallFailure.INVALID, InstallFailure.from(PackageInstaller.STATUS_FAILURE_INVALID, null))
        assertEquals(InstallFailure.NO_SPACE, InstallFailure.from(PackageInstaller.STATUS_FAILURE_STORAGE, null))
        assertEquals(InstallFailure.INCOMPATIBLE, InstallFailure.from(PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, null))
        assertEquals(InstallFailure.BLOCKED, InstallFailure.from(PackageInstaller.STATUS_FAILURE_BLOCKED, null))
        assertEquals(InstallFailure.UNKNOWN, InstallFailure.from(PackageInstaller.STATUS_FAILURE_TIMEOUT, null))
        assertEquals(InstallFailure.UNKNOWN, InstallFailure.from(PackageInstaller.STATUS_FAILURE, null))
    }

}
