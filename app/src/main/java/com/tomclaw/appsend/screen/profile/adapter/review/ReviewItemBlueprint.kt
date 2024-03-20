package com.tomclaw.appsend.screen.profile.adapter.review

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ReviewItemBlueprint(override val presenter: ItemPresenter<ReviewItemView, ReviewItem>) :
    ItemBlueprint<ReviewItemView, ReviewItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_review_item,
        creator = { _, view -> ReviewItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ReviewItem

}
