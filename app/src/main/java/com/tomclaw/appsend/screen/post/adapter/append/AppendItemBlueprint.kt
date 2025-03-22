package com.tomclaw.appsend.screen.post.adapter.append

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class AppendItemBlueprint(
    override val presenter: ItemPresenter<AppendItemView, AppendItem>,
) :
    ItemBlueprint<AppendItemView, AppendItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.post_block_append_item,
            creator = { _, view -> AppendItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is AppendItem

}
