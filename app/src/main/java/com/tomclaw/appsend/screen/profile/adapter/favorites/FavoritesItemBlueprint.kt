package com.tomclaw.appsend.screen.profile.adapter.favorites

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class FavoritesItemBlueprint(override val presenter: ItemPresenter<FavoritesItemView, FavoritesItem>) :
    ItemBlueprint<FavoritesItemView, FavoritesItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_favorites,
        creator = { _, view -> FavoritesItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is FavoritesItem

}
