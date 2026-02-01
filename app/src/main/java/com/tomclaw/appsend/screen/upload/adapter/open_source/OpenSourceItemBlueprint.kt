package com.tomclaw.appsend.screen.upload.adapter.open_source

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class OpenSourceItemBlueprint(override val presenter: ItemPresenter<OpenSourceItemView, OpenSourceItem>) :
    ItemBlueprint<OpenSourceItemView, OpenSourceItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_open_source,
        creator = { _, view -> OpenSourceItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is OpenSourceItem

}
