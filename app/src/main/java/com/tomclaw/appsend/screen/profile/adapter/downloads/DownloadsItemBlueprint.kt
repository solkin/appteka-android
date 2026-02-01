package com.tomclaw.appsend.screen.profile.adapter.downloads

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class DownloadsItemBlueprint(override val presenter: ItemPresenter<DownloadsItemView, DownloadsItem>) :
    ItemBlueprint<DownloadsItemView, DownloadsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_downloads,
        creator = { _, view -> DownloadsItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is DownloadsItem

}
