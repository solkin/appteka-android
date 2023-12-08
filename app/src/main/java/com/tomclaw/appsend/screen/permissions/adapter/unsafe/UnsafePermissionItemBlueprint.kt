package com.tomclaw.appsend.screen.permissions.adapter.unsafe

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class UnsafePermissionItemBlueprint(override val presenter: ItemPresenter<UnsafePermissionItemView, UnsafePermissionItem>) :
    ItemBlueprint<UnsafePermissionItemView, UnsafePermissionItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.permission_unsafe,
        creator = { _, view -> UnsafePermissionItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is UnsafePermissionItem

}
