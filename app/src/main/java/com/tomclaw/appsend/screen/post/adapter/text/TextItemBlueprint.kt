package com.tomclaw.appsend.screen.post.adapter.text

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class TextItemBlueprint(override val presenter: ItemPresenter<TextItemView, TextItem>) :
    ItemBlueprint<TextItemView, TextItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.post_block_text,
        creator = { _, view -> TextItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is TextItem

}
