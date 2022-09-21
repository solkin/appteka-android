package com.tomclaw.appsend.screen.details.adapter.user_review

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class UserReviewItemBlueprint(override val presenter: ItemPresenter<UserReviewItemView, UserReviewItem>) :
    ItemBlueprint<UserReviewItemView, UserReviewItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.details_block_review,
            creator = { _, view -> UserReviewItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is UserReviewItem

}
