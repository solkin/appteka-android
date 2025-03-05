package com.tomclaw.appsend.screen.feed.adapter.favorite

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import com.tomclaw.appsend.screen.feed.adapter.ScreenshotsAdapter

class FavoriteItemBlueprint(
    override val presenter: ItemPresenter<FavoriteItemView, FavoriteItem>,
    listener: ItemListener
) : ItemBlueprint<FavoriteItemView, FavoriteItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_favorite,
        creator = { _, view ->
            FavoriteItemViewHolder(view, ScreenshotsAdapter(listener))
        }
    )

    override fun isRelevantItem(item: Item) = item is FavoriteItem

}
