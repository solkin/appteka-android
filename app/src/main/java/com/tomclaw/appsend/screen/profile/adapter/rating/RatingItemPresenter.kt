package com.tomclaw.appsend.screen.profile.adapter.rating

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.ratings.RatingItemListener
import java.text.DateFormat

class RatingItemPresenter(
    private val dateFormatter: DateFormat,
    private val listener: RatingItemListener,
) : ItemPresenter<RatingItemView, RatingItem> {

    override fun bindView(view: RatingItemView, item: RatingItem, position: Int) {
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
