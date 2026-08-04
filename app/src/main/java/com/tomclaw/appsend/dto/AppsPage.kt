package com.tomclaw.appsend.dto

/**
 * One page of a catalog listing, together with what the server left out
 * of it. A filtered list that cannot say it was filtered is
 * indistinguishable from a list that simply has nothing in it, and the
 * app someone remembers is gone with no thread to pull.
 */
data class AppsPage(
    val entries: List<AppEntity>,
    /** Content flag codes withheld for this viewer; empty for everyone else. */
    val contentFilter: List<String> = emptyList(),
)
