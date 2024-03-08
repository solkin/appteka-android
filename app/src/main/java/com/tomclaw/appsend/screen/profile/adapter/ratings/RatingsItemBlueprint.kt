package com.tomclaw.appsend.screen.profile.adapter.ratings

import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R
import dagger.Lazy

class RatingsItemBlueprint(
    override val presenter: ItemPresenter<RatingsItemView, RatingsItem>,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val binder: Lazy<ItemBinder>,
) :
    ItemBlueprint<RatingsItemView, RatingsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_ratings,
        creator = { _, view ->
            RatingsItemViewHolder(
                view,
                SimpleRecyclerAdapter(adapterPresenter.get(), binder.get())
            )
        }
    )

    override fun isRelevantItem(item: Item) = item is RatingsItem

}
