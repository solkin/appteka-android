package com.tomclaw.appsend.screen.details.adapter.screenshots

import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.blueprint.ItemBlueprint
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.blueprint.ViewHolderBuilder
import com.tomclaw.appsend.R

class ScreenshotsItemBlueprint(
    override val presenter: ItemPresenter<ScreenshotsItemView, ScreenshotsItem>,
    private val screenshotsAdapter: ScreenshotsAdapter,
) :
    ItemBlueprint<ScreenshotsItemView, ScreenshotsItem> {

    override val viewHolderProvider =
        ViewHolderBuilder.ViewHolderProvider(
            layoutId = R.layout.details_block_screenshots,
            creator = { _, view -> ScreenshotsItemViewHolder(view, screenshotsAdapter) }
        )

    override fun isRelevantItem(item: Item) = item is ScreenshotsItem

}
