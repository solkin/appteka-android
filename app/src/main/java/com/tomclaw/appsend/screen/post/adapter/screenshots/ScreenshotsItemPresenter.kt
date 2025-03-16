package com.tomclaw.appsend.screen.post.adapter.screenshots

import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.post.adapter.ItemListener
import dagger.Lazy

class ScreenshotsItemPresenter(
    private val listener: ItemListener,
    private val adapterPresenter: Lazy<AdapterPresenter>,
) : ItemPresenter<ScreenshotsItemView, ScreenshotsItem> {

    override fun bindView(view: ScreenshotsItemView, item: ScreenshotsItem, position: Int) {
        val dataSource = ListDataSource(item.items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view.notifyChanged()
    }

}
