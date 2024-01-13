package com.tomclaw.appsend.screen.upload.adapter.screen_append

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ScreenAppendItemBlueprint(
    override val presenter: ItemPresenter<ScreenAppendItemView, ScreenAppendItem>,
) :
    ItemBlueprint<ScreenAppendItemView, ScreenAppendItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.upload_block_screenshot_append_item,
            creator = { _, view -> ScreenAppendItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is ScreenAppendItem

}
