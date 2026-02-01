package com.tomclaw.appsend.screen.details.adapter.screenshot

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
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
