package com.tomclaw.appsend.screen.profile.adapter.moderation

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ModerationItemBlueprint(override val presenter: ItemPresenter<ModerationItemView, ModerationItem>) :
    ItemBlueprint<ModerationItemView, ModerationItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_moderation,
        creator = { _, view -> ModerationItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ModerationItem

}

