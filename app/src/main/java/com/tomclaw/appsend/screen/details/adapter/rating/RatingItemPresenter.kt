package com.tomclaw.appsend.screen.details.adapter.rating

import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.util.adapter.ItemPresenter
import java.text.DateFormat
import java.util.Locale

class RatingItemPresenter(
    private val dateFormatter: DateFormat,
    private val locale: Locale,
    private val listener: ItemListener,
) : ItemPresenter<RatingItemView, RatingItem> {

    override fun bindView(view: RatingItemView, item: RatingItem, position: Int) {
        item.user.icon?.let(view::setUserIcon)
        view.setUserBadge(item.user.primaryBadge)
        val name = item.user.name.takeIf { !it.isNullOrBlank() }
            ?: item.user.icon?.label?.get(locale.language)
            ?: item.user.icon?.label?.get(DEFAULT_LOCALE).orEmpty()
        view.setUserName(name)
        view.setRating(item.score.toFloat())
        val date: String = dateFormatter.format(item.time)
        view.setDate(date)
        view.setComment(item.text)
        view.setOnClickListener { listener.onScoresClick() }
    }

}
