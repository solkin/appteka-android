package com.tomclaw.appsend.screen.post.adapter.ribbon

import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R
import dagger.Lazy

class RibbonItemBlueprint(
    override val presenter: ItemPresenter<RibbonItemView, RibbonItem>,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val binder: Lazy<ItemBinder>,
) :
    ItemBlueprint<RibbonItemView, RibbonItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.post_block_ribbon,
            creator = { _, view ->
                RibbonItemViewHolder(
                    view,
                    SimpleRecyclerAdapter(adapterPresenter.get(), binder.get())
                )
            }
        )

    override fun isRelevantItem(item: Item) = item is RibbonItem

}
