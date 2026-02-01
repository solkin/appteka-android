package com.tomclaw.appsend.screen.details.adapter.security

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class SecurityItemBlueprint(override val presenter: ItemPresenter<SecurityItemView, SecurityItem>) :
    ItemBlueprint<SecurityItemView, SecurityItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_security,
        creator = { _, view -> SecurityItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SecurityItem

}

