package com.tomclaw.appsend.screen.profile.adapter.moderation

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class ModerationItemBlueprint(override val presenter: ItemPresenter<ModerationItemView, ModerationItem>) :
    ItemBlueprint<ModerationItemView, ModerationItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_moderation,
        creator = { _, view -> ModerationItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ModerationItem

}

