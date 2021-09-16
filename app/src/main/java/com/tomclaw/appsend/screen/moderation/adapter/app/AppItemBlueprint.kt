package com.tomclaw.appsend.screen.moderation.adapter.app

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class AppItemBlueprint(override val presenter: ItemPresenter<AppItemView, AppItem>) :
    ItemBlueprint<AppItemView, AppItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.app_item,
        creator = { _, view -> AppItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is AppItem

}
