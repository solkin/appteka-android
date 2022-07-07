package com.tomclaw.appsend.screen.details.adapter.scores

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ScoresItemBlueprint(override val presenter: ItemPresenter<ScoresItemView, ScoresItem>) :
    ItemBlueprint<ScoresItemView, ScoresItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_scores,
        creator = { _, view -> ScoresItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ScoresItem

}
