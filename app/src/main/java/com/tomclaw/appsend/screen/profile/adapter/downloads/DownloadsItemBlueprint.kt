package com.tomclaw.appsend.screen.profile.adapter.downloads

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class DownloadsItemBlueprint(override val presenter: ItemPresenter<DownloadsItemView, DownloadsItem>) :
    ItemBlueprint<DownloadsItemView, DownloadsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_downloads,
        creator = { _, view -> DownloadsItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is DownloadsItem

}
