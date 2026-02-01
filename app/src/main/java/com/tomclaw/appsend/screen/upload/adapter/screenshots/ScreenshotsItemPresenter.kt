package com.tomclaw.appsend.screen.upload.adapter.screenshots

import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.upload.adapter.ItemListener
import dagger.Lazy

class ScreenshotsItemPresenter(
    private val listener: ItemListener,
    private val adapterPresenter: Lazy<AdapterPresenter>,
) : ItemPresenter<ScreenshotsItemView, ScreenshotsItem> {

    override fun bindView(view: ScreenshotsItemView, item: ScreenshotsItem, position: Int) {
        adapterPresenter.get().onDataSourceChanged(item.items)
        view.notifyChanged()
    }

}
