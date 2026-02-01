package com.tomclaw.appsend.screen.profile.adapter.reviews

import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.ItemPresenter
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
        adapterPresenter.get().onDataSourceChanged(item.items)
        view.notifyChanged()
    }

    override fun onRatingClick(item: ReviewItem) {
        listener.onAppClick(item.appId, item.title)
    }

}
