package com.tomclaw.appsend.screen.profile.adapter.uploads

import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R
import dagger.Lazy

class UploadsItemBlueprint(
    override val presenter: ItemPresenter<UploadsItemView, UploadsItem>,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val binder: Lazy<ItemBinder>,
) :
    ItemBlueprint<UploadsItemView, UploadsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_uploads,
        creator = { _, view ->
            UploadsItemViewHolder(
                view,
                SimpleRecyclerAdapter(adapterPresenter.get(), binder.get())
            )
        }
    )

    override fun isRelevantItem(item: Item) = item is UploadsItem

}
