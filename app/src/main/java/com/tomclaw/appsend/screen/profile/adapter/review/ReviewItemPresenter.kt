package com.tomclaw.appsend.screen.profile.adapter.review

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.reviews.ReviewItemListener
import java.text.DateFormat

class ReviewItemPresenter(
    private val dateFormatter: DateFormat,
    private val listener: ReviewItemListener,
) : ItemPresenter<ReviewItemView, ReviewItem> {

    override fun bindView(view: ReviewItemView, item: ReviewItem, position: Int) {
        view.setIcon(item.icon)
        view.setTitle(item.title)
        view.setVersion(item.version)
        view.setRating(item.rating)
        val date: String = dateFormatter.format(item.time)
        view.setDate(date)
        view.setReview(item.text)
        view.setOnClickListener { listener.onRatingClick(item) }
    }

}
