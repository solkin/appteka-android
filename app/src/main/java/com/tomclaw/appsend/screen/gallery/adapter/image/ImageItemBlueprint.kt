package com.tomclaw.appsend.screen.gallery.adapter.image

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class ImageItemBlueprint(override val presenter: ItemPresenter<ImageItemView, ImageItem>) :
    ItemBlueprint<ImageItemView, ImageItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.gallery_image_item,
            creator = { _, view -> ImageItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is ImageItem

}
