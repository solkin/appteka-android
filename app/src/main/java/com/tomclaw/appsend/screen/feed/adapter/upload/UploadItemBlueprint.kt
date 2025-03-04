package com.tomclaw.appsend.screen.feed.adapter.upload

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class UploadItemBlueprint(override val presenter: ItemPresenter<UploadItemView, UploadItem>) :
    ItemBlueprint<UploadItemView, UploadItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_upload,
        creator = { _, view -> UploadItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is UploadItem

}
