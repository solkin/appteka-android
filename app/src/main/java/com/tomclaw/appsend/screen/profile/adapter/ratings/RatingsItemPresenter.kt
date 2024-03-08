package com.tomclaw.appsend.screen.profile.adapter.ratings

import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.profile.adapter.ItemListener
import com.tomclaw.appsend.screen.profile.adapter.rating.RatingItem

class RatingsItemPresenter(
    private val listener: ItemListener,
    private val adapterPresenter: dagger.Lazy<AdapterPresenter>,
) : ItemPresenter<RatingsItemView, RatingsItem>, RatingItemListener {

    private var ratings = emptyList<RatingItem>()

    override fun bindView(view: RatingsItemView, item: RatingsItem, position: Int) {
        view.setRatingsCount(item.count.toString())

        ratings = item.items
        val dataSource = ListDataSource(item.items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view.notifyChanged()
    }

    override fun onRatingClick(item: RatingItem) {
        listener.onRatingClick(item)
    }

}
