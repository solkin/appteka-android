package com.tomclaw.appsend.screen.feed.adapter.text

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class TextItemBlueprint(override val presenter: ItemPresenter<PostItemView, TextItem>) :
    ItemBlueprint<PostItemView, TextItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.publisher_item,
        creator = { _, view -> PostItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is TextItem

}
