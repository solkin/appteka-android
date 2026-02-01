package com.tomclaw.appsend.screen.upload.adapter.screen_append

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
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
