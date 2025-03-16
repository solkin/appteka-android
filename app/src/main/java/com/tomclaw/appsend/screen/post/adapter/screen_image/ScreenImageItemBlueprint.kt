package com.tomclaw.appsend.screen.post.adapter.screen_image

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ScreenImageItemBlueprint(
    override val presenter: ItemPresenter<ScreenImageItemView, ScreenImageItem>,
) :
    ItemBlueprint<ScreenImageItemView, ScreenImageItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.post_block_screenshot_image_item,
            creator = { _, view -> ScreenImageItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is ScreenImageItem

}
