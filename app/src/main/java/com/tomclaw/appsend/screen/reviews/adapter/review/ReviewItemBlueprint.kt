package com.tomclaw.appsend.screen.reviews.adapter.review

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
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
