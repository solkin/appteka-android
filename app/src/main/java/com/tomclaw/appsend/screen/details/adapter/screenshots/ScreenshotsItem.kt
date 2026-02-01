package com.tomclaw.appsend.screen.details.adapter.screenshots

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.screen.details.adapter.screenshot.ScreenshotItem

data class ScreenshotsItem(
    override val id: Long,
    val items: List<ScreenshotItem>,
) : Item
