package com.tomclaw.appsend.screen.details.adapter.security

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class SecurityItemBlueprint(override val presenter: ItemPresenter<SecurityItemView, SecurityItem>) :
    ItemBlueprint<SecurityItemView, SecurityItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_security,
        creator = { _, view -> SecurityItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SecurityItem

}

