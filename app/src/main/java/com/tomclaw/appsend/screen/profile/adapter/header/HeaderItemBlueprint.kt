package com.tomclaw.appsend.screen.profile.adapter.header

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class HeaderItemBlueprint(override val presenter: ItemPresenter<HeaderItemView, HeaderItem>) :
    ItemBlueprint<HeaderItemView, HeaderItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_header,
        creator = { _, view -> HeaderItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is HeaderItem

}
