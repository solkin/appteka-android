package com.tomclaw.appsend.screen.details.adapter.play

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class PlayItemBlueprint(override val presenter: ItemPresenter<PlayItemView, PlayItem>) :
    ItemBlueprint<PlayItemView, PlayItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_play,
        creator = { _, view -> PlayItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PlayItem

}
