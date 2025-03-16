package com.tomclaw.appsend.screen.post.adapter.screenshots

import com.avito.konveyor.blueprint.Item

data class ScreenshotsItem(
    override val id: Long,
    val items: List<Item>,
) : Item
