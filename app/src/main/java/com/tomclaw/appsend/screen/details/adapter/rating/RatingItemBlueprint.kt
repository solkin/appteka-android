package com.tomclaw.appsend.screen.details.adapter.rating

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class RatingItemBlueprint(override val presenter: ItemPresenter<RatingItemView, RatingItem>) :
    ItemBlueprint<RatingItemView, RatingItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.details_block_rating,
            creator = { _, view -> RatingItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is RatingItem

}
