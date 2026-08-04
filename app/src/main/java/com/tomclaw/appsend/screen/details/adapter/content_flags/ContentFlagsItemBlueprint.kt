package com.tomclaw.appsend.screen.details.adapter.content_flags

import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder

class ContentFlagsItemBlueprint(
    override val presenter: ItemPresenter<ContentFlagsItemView, ContentFlagsItem>
) : ItemBlueprint<ContentFlagsItemView, ContentFlagsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_content_flags,
        creator = { _, view -> ContentFlagsItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ContentFlagsItem

}
