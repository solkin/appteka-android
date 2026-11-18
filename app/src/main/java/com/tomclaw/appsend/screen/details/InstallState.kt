package com.tomclaw.appsend.screen.details

import android.os.Parcelable
import com.tomclaw.appsend.screen.details.api.AppEdition
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.util.NOT_INSTALLED
import kotlinx.parcelize.Parcelize

/**
 * The installed app as the details screen needs it: its version code
 * ([NOT_INSTALLED] if there is none), its edition key, null if unknown, and
 * whether removing it lets another edition in (a system app stays).
 */
@Parcelize
data class InstalledApp(
    val versionCode: Int,
    val editionKey: String?,
    val removable: Boolean,
) : Parcelable {

    val isInstalled: Boolean
        get() = versionCode != NOT_INSTALLED

    companion object {
        val NONE = InstalledApp(NOT_INSTALLED, editionKey = null, removable = false)
    }

}

/** What the installed app is to the build on screen. */
enum class InstallState {
    NOT_INSTALLED,
    OLDER,
    SAME,
    NEWER,

    /** Signed with another key: the build neither updates nor reinstalls it. */
    OTHER_EDITION,
}

/**
 * A key unknown on either side (an unchecked build, an unreadable signature)
 * says nothing about editions, so versions decide, as before editions.
 */
fun installState(installed: InstalledApp, versionCode: Int, editionKey: String?): InstallState {
    return when {
        !installed.isInstalled -> InstallState.NOT_INSTALLED
        editionKey != null && installed.editionKey != null && installed.editionKey != editionKey ->
            InstallState.OTHER_EDITION

        installed.versionCode < versionCode -> InstallState.OLDER
        installed.versionCode == versionCode -> InstallState.SAME
        else -> InstallState.NEWER
    }
}

fun Details.installState(installed: InstalledApp): InstallState {
    return installState(installed, info.versionCode, currentEdition()?.key)
}

/** The edition of the build on screen, null if the server sent none. */
fun Details.currentEdition(): AppEdition? = editions?.firstOrNull { it.current == true }

/** The listed edition signed with [key]. */
fun Details.editionOf(key: String?): AppEdition? {
    key ?: return null
    return editions?.firstOrNull { it.key == key }
}
