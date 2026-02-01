package com.tomclaw.appsend.screen.post.adapter.reactions

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.adapter.ReactionsAdapter

class ReactionsItemBlueprint(
    override val presenter: ItemPresenter<ReactionsItemView, ReactionsItem>
) : ItemBlueprint<ReactionsItemView, ReactionsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.post_block_reactions,
        creator = { _, view ->
            val reactionsAdapter = ReactionsAdapter()
            ReactionsItemViewHolder(view, reactionsAdapter)
        }
    )

    override fun isRelevantItem(item: Item) = item is ReactionsItem

}
