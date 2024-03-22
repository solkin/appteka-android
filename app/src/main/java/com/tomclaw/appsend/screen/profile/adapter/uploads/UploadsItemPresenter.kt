package com.tomclaw.appsend.screen.profile.adapter.uploads

import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.profile.adapter.ItemListener
import com.tomclaw.appsend.screen.profile.adapter.app.AppItem

class UploadsItemPresenter(
    private val listener: ItemListener,
    private val adapterPresenter: dagger.Lazy<AdapterPresenter>,
) : ItemPresenter<UploadsItemView, UploadsItem>, AppItemListener {

    private var uploads = emptyList<AppItem>()

    override fun bindView(view: UploadsItemView, item: UploadsItem, position: Int) {
        view.setUploadsCount(item.uploads.toString())
        view.setDownloadsCount(item.downloads.toString())
        view.setOnClickListener { listener.onUploadsClick(item.userId) }
        view.setOnNextPageListener {
            listener.onNextPage(uploads.last()) { items ->
                uploads = uploads + items
                onUploadsChanged(view)
            }
        }

        uploads = item.items
        onUploadsChanged(view)
    }

    private fun onUploadsChanged(view: UploadsItemView) {
        val dataSource = ListDataSource(uploads)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view.notifyChanged()
    }

    override fun onAppClick(app: AppItem) {
        listener.onAppClick(app.appId, app.title)
    }

}
