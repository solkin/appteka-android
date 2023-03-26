package com.tomclaw.appsend.screen.upload.adapter.submit

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class SubmitItemBlueprint(override val presenter: ItemPresenter<SubmitItemView, SubmitItem>) :
    ItemBlueprint<SubmitItemView, SubmitItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_submit,
        creator = { _, view -> SubmitItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SubmitItem

}
