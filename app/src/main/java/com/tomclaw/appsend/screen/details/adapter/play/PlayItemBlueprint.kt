package com.tomclaw.appsend.screen.details.adapter.play

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class PlayItemBlueprint(override val presenter: ItemPresenter<PlayItemView, PlayItem>) :
    ItemBlueprint<PlayItemView, PlayItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_play,
        creator = { _, view -> PlayItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PlayItem

}
