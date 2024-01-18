package com.tomclaw.appsend.screen.details.adapter.screenshots

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.details.adapter.screenshot.ScreenshotItem

data class ScreenshotsItem(
    override val id: Long,
    val items: List<ScreenshotItem>,
) : Item
