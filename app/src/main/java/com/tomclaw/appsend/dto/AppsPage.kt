package com.tomclaw.appsend.dto

import com.tomclaw.appsend.core.content.ContentFlag

/**
 * One page of a catalog listing, together with what the server left out
 * of it. A filtered list that cannot say it was filtered is
 * indistinguishable from a list that simply has nothing in it, and the
 * app someone remembers is gone with no thread to pull.
 */
data class AppsPage(
    val entries: List<AppEntity>,
    /** What was withheld for this viewer, worded; empty for everyone else. */
    val contentFilter: List<ContentFlag> = emptyList(),
)
