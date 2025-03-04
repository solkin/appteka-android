package com.tomclaw.appsend.screen.feed.adapter.favorite

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class FavoriteItemBlueprint(override val presenter: ItemPresenter<FavoriteItemView, FavoriteItem>) :
    ItemBlueprint<FavoriteItemView, FavoriteItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_favorite,
        creator = { _, view -> FavoriteItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is FavoriteItem

}
