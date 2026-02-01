package com.tomclaw.appsend.screen.details.adapter.screenshots

import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.util.adapter.ItemBlueprint
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.util.adapter.ViewHolderBuilder
import com.tomclaw.appsend.R
import dagger.Lazy

class ScreenshotsItemBlueprint(
    override val presenter: ItemPresenter<ScreenshotsItemView, ScreenshotsItem>,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val binder: Lazy<ItemBinder>,
) :
    ItemBlueprint<ScreenshotsItemView, ScreenshotsItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.details_block_screenshots,
            creator = { _, view ->
                ScreenshotsItemViewHolder(
                    view,
                    SimpleRecyclerAdapter(adapterPresenter.get(), binder.get())
                )
            }
        )

    override fun isRelevantItem(item: Item) = item is ScreenshotsItem

}
