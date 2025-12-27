package com.tomclaw.appsend.screen.upload.adapter.prefill_version

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
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
