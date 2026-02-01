package com.tomclaw.appsend.screen.details.adapter.user_review

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
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
