package com.tomclaw.appsend.screen.profile.adapter.reviews

import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.ItemPresenter
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.profile.adapter.ItemListener
import com.tomclaw.appsend.screen.profile.adapter.review.ReviewItem

class ReviewsItemPresenter(
    private val listener: ItemListener,
    private val adapterPresenter: dagger.Lazy<AdapterPresenter>,
) : ItemPresenter<ReviewsItemView, ReviewsItem>, ReviewItemListener {

    private var ratings = emptyList<ReviewItem>()

    override fun bindView(view: ReviewsItemView, item: ReviewsItem, position: Int) {
        view.setRatingsCount(item.count.toString())
        view.setOnClickListener { listener.onRatingsClick() }

        ratings = item.items
        val dataSource = ListDataSource(item.items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view.notifyChanged()
    }

    override fun onRatingClick(item: ReviewItem) {
        listener.onAppClick(item.appId, item.title)
    }

}
