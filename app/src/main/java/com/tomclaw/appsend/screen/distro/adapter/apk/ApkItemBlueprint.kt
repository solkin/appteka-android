package com.tomclaw.appsend.screen.distro.adapter.apk

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class ApkItemBlueprint(override val presenter: ItemPresenter<ApkItemView, ApkItem>) :
    ItemBlueprint<ApkItemView, ApkItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.distro_item,
        creator = { _, view -> ApkItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is ApkItem

}
