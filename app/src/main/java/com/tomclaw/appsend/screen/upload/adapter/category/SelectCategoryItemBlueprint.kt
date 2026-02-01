package com.tomclaw.appsend.screen.upload.adapter.category

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R

class SelectCategoryItemBlueprint(override val presenter: ItemPresenter<SelectCategoryItemView, SelectCategoryItem>) :
    ItemBlueprint<SelectCategoryItemView, SelectCategoryItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_select_category,
        creator = { _, view -> SelectCategoryItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SelectCategoryItem

}
