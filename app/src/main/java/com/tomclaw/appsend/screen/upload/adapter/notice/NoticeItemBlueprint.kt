package com.tomclaw.appsend.screen.upload.adapter.notice

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class NoticeItemBlueprint(override val presenter: ItemPresenter<NoticeItemView, NoticeItem>) :
    ItemBlueprint<NoticeItemView, NoticeItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_notice,
        creator = { _, view -> NoticeItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is NoticeItem

}
