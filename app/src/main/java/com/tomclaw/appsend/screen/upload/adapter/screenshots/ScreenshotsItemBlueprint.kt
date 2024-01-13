package com.tomclaw.appsend.screen.upload.adapter.screenshots

import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
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
            layoutId = R.layout.upload_block_screenshots,
            creator = { _, view ->
                ScreenshotsItemViewHolder(
                    view,
                    SimpleRecyclerAdapter(adapterPresenter.get(), binder.get())
                )
            }
        )

    override fun isRelevantItem(item: Item) = item is ScreenshotsItem

}
