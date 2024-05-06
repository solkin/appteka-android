package com.tomclaw.appsend.screen.ratings.adapter.rating

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class RatingItemBlueprint(override val presenter: ItemPresenter<RatingItemView, RatingItem>) :
    ItemBlueprint<RatingItemView, RatingItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.rating_item_legacy,
            creator = { _, view -> RatingItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is RatingItem

}
