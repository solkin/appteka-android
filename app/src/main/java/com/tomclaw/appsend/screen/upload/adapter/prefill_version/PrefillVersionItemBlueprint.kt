package com.tomclaw.appsend.screen.upload.adapter.prefill_version

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class PrefillVersionItemBlueprint(
    override val presenter: ItemPresenter<PrefillVersionItemView, PrefillVersionItem>
) : ItemBlueprint<PrefillVersionItemView, PrefillVersionItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_prefill_version,
        creator = { _, view -> PrefillVersionItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is PrefillVersionItem

}
