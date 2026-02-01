package com.tomclaw.appsend.screen.feed.adapter.upload

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.adapter.ItemListener
import com.tomclaw.appsend.screen.feed.adapter.ReactionsAdapter
import com.tomclaw.appsend.screen.feed.adapter.ScreenshotsAdapter

class UploadItemBlueprint(
    override val presenter: ItemPresenter<UploadItemView, UploadItem>,
    listener: ItemListener
) : ItemBlueprint<UploadItemView, UploadItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.feed_item_upload,
        creator = { _, view ->
            UploadItemViewHolder(view, ScreenshotsAdapter(listener), ReactionsAdapter())
        }
    )

    override fun isRelevantItem(item: Item) = item is UploadItem

}
