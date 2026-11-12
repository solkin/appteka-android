package com.tomclaw.appsend.screen.installed

import com.tomclaw.appsend.screen.installed.api.UpdateEntity

/**
 * Update check result. [otherEditions] are newer builds of another
 * publisher: they install only after the installed app is removed, so they
 * are never offered as updates.
 */
data class InstalledUpdates(
    val entries: List<UpdateEntity>,
    val otherEditions: List<UpdateEntity>,
) {

    companion object {
        val EMPTY = InstalledUpdates(entries = emptyList(), otherEditions = emptyList())
    }

}
