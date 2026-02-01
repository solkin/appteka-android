package com.tomclaw.appsend.screen.post.adapter.ribbon

import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.post.adapter.ItemListener
import dagger.Lazy

class RibbonItemPresenter(
    private val listener: ItemListener,
    private val adapterPresenter: Lazy<AdapterPresenter>,
) : ItemPresenter<RibbonItemView, RibbonItem> {

    override fun bindView(view: RibbonItemView, item: RibbonItem, position: Int) {
        adapterPresenter.get().onDataSourceChanged(item.items)
        view.notifyChanged()
    }

}
