package com.tomclaw.appsend.screen.details.adapter.permissions

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class PermissionsItemBlueprint(override val presenter: ItemPresenter<PermissionsItemView, PermissionsItem>) :
    ItemBlueprint<PermissionsItemView, PermissionsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_permissions,
        creator = { _, view -> PermissionsItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PermissionsItem

}
