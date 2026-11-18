package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.screen.details.api.AppEdition
import com.tomclaw.appsend.screen.details.api.AppVersion
import com.tomclaw.appsend.screen.details.api.Details

/**
 * Versions of one edition; [edition] is null for the flat list of a server
 * that sends no editions.
 */
data class EditionVersions(
    val edition: AppEdition?,
    val installed: Boolean,
    val versions: List<VersionMark>,
)

data class VersionMark(
    val version: AppVersion,
    // Installs over the installed app, or over the build on screen when
    // nothing is installed, as an update.
    val newer: Boolean,
)

/**
 * Versions grouped by edition, in the server's order. "Newer" is only ever
 * said inside one edition: the installed one, or the one on screen when the
 * installed key is unknown. Builds without a known key update nothing.
 */
fun Details.versionsByEdition(installed: InstalledApp): List<EditionVersions> {
    val editions = editions ?: return listOf(flatVersions())
    val (key, versionCode) = if (installed.isInstalled && installed.editionKey != null) {
        installed.editionKey to installed.versionCode
    } else {
        currentEdition()?.key to info.versionCode
    }
    return editions.map { edition ->
        val reference = key != null && edition.key == key
        EditionVersions(
            edition = edition,
            installed = installed.isInstalled && edition.key != null && edition.key == installed.editionKey,
            versions = edition.versions.orEmpty().map { version ->
                VersionMark(version, newer = reference && version.verCode > versionCode)
            },
        )
    }
}

private fun Details.flatVersions(): EditionVersions {
    return EditionVersions(
        edition = null,
        installed = false,
        versions = versions.orEmpty()
            .sortedByDescending { it.verCode }
            .map { VersionMark(it, newer = it.verCode > info.versionCode) },
    )
}
