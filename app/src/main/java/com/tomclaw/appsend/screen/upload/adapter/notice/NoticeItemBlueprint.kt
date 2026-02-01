package com.tomclaw.appsend.screen.upload.adapter.notice

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class NoticeItemBlueprint(override val presenter: ItemPresenter<NoticeItemView, NoticeItem>) :
    ItemBlueprint<NoticeItemView, NoticeItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_notice,
        creator = { _, view -> NoticeItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is NoticeItem

}
