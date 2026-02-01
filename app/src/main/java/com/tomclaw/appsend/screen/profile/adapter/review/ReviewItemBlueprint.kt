package com.tomclaw.appsend.screen.profile.adapter.review

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class ReviewItemBlueprint(override val presenter: ItemPresenter<ReviewItemView, ReviewItem>) :
    ItemBlueprint<ReviewItemView, ReviewItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_review_item,
        creator = { _, view -> ReviewItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ReviewItem

}
