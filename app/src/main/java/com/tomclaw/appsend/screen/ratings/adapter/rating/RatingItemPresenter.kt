package com.tomclaw.appsend.screen.ratings.adapter.rating

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.ratings.adapter.ItemListener
import java.text.DateFormat

class RatingItemPresenter(
    private val dateFormatter: DateFormat,
    private val listener: ItemListener,
) : ItemPresenter<RatingItemView, RatingItem> {

    override fun bindView(view: RatingItemView, item: RatingItem, position: Int) {
        with(item) {
            if (hasMore) {
                hasMore = false
                hasProgress = true
                hasError = false
                listener.onLoadMore(this)
            }
        }

        view.setUserIcon(item.userIcon)
        view.setUserName(item.userName)
        view.setRating(item.score.toFloat())
        val date: String = dateFormatter.format(item.time)
        view.setDate(date)
        view.setComment(item.text)
        view.setOnRatingClickListener { listener.onItemClick(item) }
        view.setOnDeleteClickListener { listener.onDeleteClick(item) }
    }

}
