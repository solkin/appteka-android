package com.tomclaw.appsend.screen.feed.adapter.post

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class PostItemBlueprint(override val presenter: ItemPresenter<PostItemView, PostItem>) :
    ItemBlueprint<PostItemView, PostItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.publisher_item,
        creator = { _, view -> PostItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PostItem

}
