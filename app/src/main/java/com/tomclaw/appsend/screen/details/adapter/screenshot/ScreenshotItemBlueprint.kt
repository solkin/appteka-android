package com.tomclaw.appsend.screen.details.adapter.screenshot

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ScreenshotItemBlueprint(
    override val presenter: ItemPresenter<ScreenshotItemView, ScreenshotItem>,
) :
    ItemBlueprint<ScreenshotItemView, ScreenshotItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.details_block_screenshot_item,
            creator = { _, view -> ScreenshotItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is ScreenshotItem

}
