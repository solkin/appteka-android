package com.tomclaw.appsend.screen.chat.adapter.loadmore

import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder

class LoadMoreItemBlueprint(override val presenter: ItemPresenter<LoadMoreItemView, LoadMoreItem>) :
    ItemBlueprint<LoadMoreItemView, LoadMoreItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.chat_item_load_more,
        creator = { _, view -> LoadMoreItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is LoadMoreItem

}
