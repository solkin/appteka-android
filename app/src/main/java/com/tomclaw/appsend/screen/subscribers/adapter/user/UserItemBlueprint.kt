package com.tomclaw.appsend.screen.subscribers.adapter.user

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class UserItemBlueprint(override val presenter: ItemPresenter<UserItemView, UserItem>) :
    ItemBlueprint<UserItemView, UserItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.store_item,
        creator = { _, view -> UserItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is UserItem

}
