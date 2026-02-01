package com.tomclaw.appsend.screen.profile.adapter.uploads

import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
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
