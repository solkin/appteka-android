package com.tomclaw.appsend.screen.details.adapter.scores

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class ScoresItemBlueprint(override val presenter: ItemPresenter<ScoresItemView, ScoresItem>) :
    ItemBlueprint<ScoresItemView, ScoresItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_scores,
        creator = { _, view -> ScoresItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ScoresItem

}
