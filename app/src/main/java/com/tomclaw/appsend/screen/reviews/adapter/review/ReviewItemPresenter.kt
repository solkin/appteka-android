package com.tomclaw.appsend.screen.reviews.adapter.review

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.reviews.adapter.ItemListener
import java.text.DateFormat

class ReviewItemPresenter(
    private val dateFormatter: DateFormat,
    private val listener: ItemListener,
) : ItemPresenter<ReviewItemView, ReviewItem> {

    override fun bindView(view: ReviewItemView, item: ReviewItem, position: Int) {
        with(item) {
            if (hasMore) {
                hasMore = false
                hasProgress = true
                hasError = false
                listener.onLoadMore(this)
            }
        }

        view.setIcon(item.icon)
        view.setTitle(item.title)
        view.setVersion(item.version)
        view.setRating(item.rating)
        val date: String = dateFormatter.format(item.time)
        view.setDate(date)
        view.setReview(item.text)
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (item.showRatingMenu) {
            view.showRatingMenu()
        } else {
            view.hideRatingMenu()
        }
        view.setOnReviewClickListener { listener.onItemClick(item) }
        view.setOnDeleteClickListener { listener.onDeleteClick(item) }
    }

}
