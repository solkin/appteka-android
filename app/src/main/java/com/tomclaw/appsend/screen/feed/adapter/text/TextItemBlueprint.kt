package com.tomclaw.appsend.screen.feed.adapter.text

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class TextItemBlueprint(override val presenter: ItemPresenter<TextItemView, TextItem>) :
    ItemBlueprint<TextItemView, TextItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_text,
        creator = { _, view -> TextItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is TextItem

}
