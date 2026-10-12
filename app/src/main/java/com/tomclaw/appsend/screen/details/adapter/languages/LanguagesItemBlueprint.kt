package com.tomclaw.appsend.screen.details.adapter.languages

import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder

class LanguagesItemBlueprint(
    override val presenter: ItemPresenter<LanguagesItemView, LanguagesItem>
) : ItemBlueprint<LanguagesItemView, LanguagesItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.details_block_languages,
        creator = { _, view -> LanguagesItemViewHolder(view) }
    )

    override fun isRelevantItem(item: Item) = item is LanguagesItem

}
