package com.tomclaw.appsend.screen.post.adapter.ribbon

import com.tomclaw.appsend.util.adapter.Item

data class RibbonItem(
    override val id: Long,
    val items: List<Item>,
) : Item
