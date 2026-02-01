package com.tomclaw.appsend.screen.feed.adapter.favorite

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import com.tomclaw.appsend.screen.feed.adapter.ReactionsAdapter
import com.tomclaw.appsend.screen.feed.adapter.ScreenshotsAdapter

class FavoriteItemBlueprint(
    override val presenter: ItemPresenter<FavoriteItemView, FavoriteItem>,
    listener: ItemListener
) : ItemBlueprint<FavoriteItemView, FavoriteItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_favorite,
        creator = { _, view ->
            FavoriteItemViewHolder(view, ScreenshotsAdapter(listener), ReactionsAdapter())
        }
    )

    override fun isRelevantItem(item: Item) = item is FavoriteItem

}
