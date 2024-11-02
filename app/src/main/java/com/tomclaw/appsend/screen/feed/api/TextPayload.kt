package com.tomclaw.appsend.screen.feed.api

import com.tomclaw.appsend.dto.Screenshot

data class TextPayload(
    val screenshots: List<Screenshot>,
    val text: String,
): PostPayload
