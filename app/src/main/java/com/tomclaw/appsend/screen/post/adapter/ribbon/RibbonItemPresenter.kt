package com.tomclaw.appsend.screen.post.adapter.ribbon

import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.post.adapter.ItemListener
import dagger.Lazy

class RibbonItemPresenter(
    private val listener: ItemListener,
    private val adapterPresenter: Lazy<AdapterPresenter>,
) : ItemPresenter<RibbonItemView, RibbonItem> {

    override fun bindView(view: RibbonItemView, item: RibbonItem, position: Int) {
        val dataSource = ListDataSource(item.items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view.notifyChanged()
    }

}
