package com.tomclaw.appsend.screen.profile.adapter.uploads

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class UploadsItemBlueprint(override val presenter: ItemPresenter<UploadsItemView, UploadsItem>) :
    ItemBlueprint<UploadsItemView, UploadsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_uploads,
        creator = { _, view -> UploadsItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is UploadsItem

}
