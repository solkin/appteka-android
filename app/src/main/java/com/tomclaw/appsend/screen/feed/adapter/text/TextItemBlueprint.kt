package com.tomclaw.appsend.screen.feed.adapter.text

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import com.tomclaw.appsend.screen.feed.adapter.ReactionsAdapter
import com.tomclaw.appsend.screen.feed.adapter.ScreenshotsAdapter

class TextItemBlueprint(
    override val presenter: ItemPresenter<TextItemView, TextItem>,
    listener: ItemListener
) : ItemBlueprint<TextItemView, TextItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_text,
        creator = { _, view -> 
            TextItemViewHolder(view, ScreenshotsAdapter(listener), ReactionsAdapter())
        }
    )

    override fun isRelevantItem(item: Item) = item is TextItem

}
