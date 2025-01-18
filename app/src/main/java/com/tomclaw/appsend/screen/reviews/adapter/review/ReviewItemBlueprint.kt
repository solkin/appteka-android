package com.tomclaw.appsend.screen.reviews.adapter.review

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.reviews.ReviewsPreferencesProvider

class ReviewItemBlueprint(
    override val presenter: ItemPresenter<ReviewItemView, ReviewItem>,
    private val preferences: ReviewsPreferencesProvider,
) :
    ItemBlueprint<ReviewItemView, ReviewItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.review_item,
        creator = { _, view -> ReviewItemViewHolder(view, preferences) }
    )

    override fun isRelevantItem(item: Item) = item is ReviewItem

}
