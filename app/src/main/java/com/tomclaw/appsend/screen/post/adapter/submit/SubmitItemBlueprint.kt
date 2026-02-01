package com.tomclaw.appsend.screen.post.adapter.submit

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class SubmitItemBlueprint(override val presenter: ItemPresenter<SubmitItemView, SubmitItem>) :
    ItemBlueprint<SubmitItemView, SubmitItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.post_block_submit,
        creator = { _, view -> SubmitItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SubmitItem

}
