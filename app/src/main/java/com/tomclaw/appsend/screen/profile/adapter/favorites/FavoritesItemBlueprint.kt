package com.tomclaw.appsend.screen.profile.adapter.favorites

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class FavoritesItemBlueprint(override val presenter: ItemPresenter<FavoritesItemView, FavoritesItem>) :
    ItemBlueprint<FavoritesItemView, FavoritesItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_favorites,
        creator = { _, view -> FavoritesItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is FavoritesItem

}
