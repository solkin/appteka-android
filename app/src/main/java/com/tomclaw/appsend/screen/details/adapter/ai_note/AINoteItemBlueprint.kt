package com.tomclaw.appsend.screen.details.adapter.ai_note

import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder

class AINoteItemBlueprint(
    override val presenter: ItemPresenter<AINoteItemView, AINoteItem>
) : ItemBlueprint<AINoteItemView, AINoteItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_ai_note,
        creator = { _, view -> AINoteItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is AINoteItem

}
