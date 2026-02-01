package com.tomclaw.appsend.screen.permissions.adapter.unsafe

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class UnsafePermissionItemBlueprint(override val presenter: ItemPresenter<UnsafePermissionItemView, UnsafePermissionItem>) :
    ItemBlueprint<UnsafePermissionItemView, UnsafePermissionItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.permission_unsafe,
        creator = { _, view -> UnsafePermissionItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is UnsafePermissionItem

}
