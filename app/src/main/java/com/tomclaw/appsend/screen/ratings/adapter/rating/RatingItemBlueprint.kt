package com.tomclaw.appsend.screen.ratings.adapter.rating

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.ratings.RatingsPreferencesProvider

class RatingItemBlueprint(
    override val presenter: ItemPresenter<RatingItemView, RatingItem>,
    private val preferences: RatingsPreferencesProvider,
) :
    ItemBlueprint<RatingItemView, RatingItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.rating_item,
            creator = { _, view -> RatingItemViewHolder(view, preferences) }
        )

    override fun isRelevantItem(item: Item) = item is RatingItem

}
