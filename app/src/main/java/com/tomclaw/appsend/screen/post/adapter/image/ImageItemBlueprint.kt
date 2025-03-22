package com.tomclaw.appsend.screen.post.adapter.image

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ImageItemBlueprint(
    override val presenter: ItemPresenter<ImageItemView, ImageItem>,
) :
    ItemBlueprint<ImageItemView, ImageItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.post_block_image_item,
            creator = { _, view -> ImageItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is ImageItem

}
