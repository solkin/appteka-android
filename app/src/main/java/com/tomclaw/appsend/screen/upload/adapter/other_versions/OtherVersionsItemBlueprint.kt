package com.tomclaw.appsend.screen.upload.adapter.other_versions

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class OtherVersionsItemBlueprint(override val presenter: ItemPresenter<OtherVersionsItemView, OtherVersionsItem>) :
    ItemBlueprint<OtherVersionsItemView, OtherVersionsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_other_versions,
        creator = { _, view -> OtherVersionsItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is OtherVersionsItem

}
