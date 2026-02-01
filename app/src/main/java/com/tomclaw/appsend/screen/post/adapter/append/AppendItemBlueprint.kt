package com.tomclaw.appsend.screen.post.adapter.append

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
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
