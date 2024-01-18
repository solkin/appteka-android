package com.tomclaw.appsend.screen.details.adapter.screenshots

import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.screenshot.ScreenshotItem

class ScreenshotsItemPresenter(
    private val listener: ItemListener,
    private val adapterPresenter: dagger.Lazy<AdapterPresenter>,
) : ItemPresenter<ScreenshotsItemView, ScreenshotsItem>, ScreenshotItemListener {

    private var screenshots = emptyList<ScreenshotItem>()

    override fun bindView(view: ScreenshotsItemView, item: ScreenshotsItem, position: Int) {
        screenshots = item.items
        val dataSource = ListDataSource(item.items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view.notifyChanged()
    }

    override fun onScreenshotClick(position: Int) {
        listener.onScreenshotClick(screenshots, position)
    }

}
