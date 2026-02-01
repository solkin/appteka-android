package com.tomclaw.appsend.screen.permissions.adapter.safe

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
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
