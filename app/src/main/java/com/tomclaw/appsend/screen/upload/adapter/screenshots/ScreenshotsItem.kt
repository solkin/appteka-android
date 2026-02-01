package com.tomclaw.appsend.screen.upload.adapter.screenshots

import com.tomclaw.appsend.util.adapter.Item

data class ScreenshotsItem(
    override val id: Long,
    val items: List<Item>,
) : Item
