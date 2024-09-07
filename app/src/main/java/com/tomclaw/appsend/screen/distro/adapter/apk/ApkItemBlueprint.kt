package com.tomclaw.appsend.screen.distro.adapter.apk

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ApkItemBlueprint(override val presenter: ItemPresenter<ApkItemView, ApkItem>) :
    ItemBlueprint<ApkItemView, ApkItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.distro_item,
        creator = { _, view -> ApkItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ApkItem

}
