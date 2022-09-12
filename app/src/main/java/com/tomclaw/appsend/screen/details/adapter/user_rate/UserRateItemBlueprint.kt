package com.tomclaw.appsend.screen.details.adapter.user_rate

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class UserRateItemBlueprint(override val presenter: ItemPresenter<UserRateItemView, UserRateItem>) :
    ItemBlueprint<UserRateItemView, UserRateItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.details_block_user_rate,
            creator = { _, view -> UserRateItemViewHolder(view) }
        )

    override fun isRelevantItem(item: Item) = item is UserRateItem

}
