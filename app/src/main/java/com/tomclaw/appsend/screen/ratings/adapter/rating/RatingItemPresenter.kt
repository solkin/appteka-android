package com.tomclaw.appsend.screen.ratings.adapter.rating

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.ratings.adapter.ItemListener
import java.text.DateFormat

class RatingItemPresenter(
    private val dateFormatter: DateFormat,
    private val listener: ItemListener,
) : ItemPresenter<RatingItemView, RatingItem> {

    override fun bindView(view: RatingItemView, item: RatingItem, position: Int) {
        view.setUserIcon(item.userIcon)
        view.setUserName(item.userName)
        view.setRating(item.score.toFloat())
        val date: String = dateFormatter.format(item.time)
        view.setDate(date)
        view.setComment(item.text)
        view.setOnClickListener { listener.onItemClick(item) }
    }

}
