package com.tomclaw.appsend.screen.profile.adapter.reviews

import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R
import dagger.Lazy

class ReviewsItemBlueprint(
    override val presenter: ItemPresenter<ReviewsItemView, ReviewsItem>,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val binder: Lazy<ItemBinder>,
) :
    ItemBlueprint<ReviewsItemView, ReviewsItem> {

    override val viewHolderProvider = ViewHolderBuilder.ViewHolderProvider(
        layoutId = R.layout.profile_block_ratings,
        creator = { _, view ->
            ReviewsItemViewHolder(
                view,
                SimpleRecyclerAdapter(adapterPresenter.get(), binder.get())
            )
        }
    )

    override fun isRelevantItem(item: Item) = item is ReviewsItem

}
