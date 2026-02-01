package com.tomclaw.appsend.screen.post.adapter.reactions

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.screen.feed.api.Reaction

data class ReactionsItem(
    override val id: Long,
    val availableReactions: List<Reaction>,
    val selectedReactionIds: Set<String>,
) : Item

