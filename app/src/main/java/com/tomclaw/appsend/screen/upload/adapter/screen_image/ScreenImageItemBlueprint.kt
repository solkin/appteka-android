package com.tomclaw.appsend.screen.upload.adapter.screen_image

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class ScreenImageItemBlueprint(
    override val presenter: ItemPresenter<ScreenImageItemView, ScreenImageItem>,
) :
    ItemBlueprint<ScreenImageItemView, ScreenImageItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.upload_block_screenshot_image_item,
            creator = { _, view -> ScreenImageItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is ScreenImageItem

}
