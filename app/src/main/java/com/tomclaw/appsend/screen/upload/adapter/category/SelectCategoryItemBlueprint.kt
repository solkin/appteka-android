package com.tomclaw.appsend.screen.upload.adapter.category

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class SelectCategoryItemBlueprint(override val presenter: ItemPresenter<SelectCategoryItemView, SelectCategoryItem>) :
    ItemBlueprint<SelectCategoryItemView, SelectCategoryItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.upload_block_select_category,
        creator = { _, view -> SelectCategoryItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is SelectCategoryItem

}
