package com.tomclaw.appsend.screen.permissions.adapter.safe

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class SafePermissionItemBlueprint(override val presenter: ItemPresenter<SafePermissionItemView, SafePermissionItem>) :
    ItemBlueprint<SafePermissionItemView, SafePermissionItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.permission_safe,
            creator = { _, view -> SafePermissionItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is SafePermissionItem

}
