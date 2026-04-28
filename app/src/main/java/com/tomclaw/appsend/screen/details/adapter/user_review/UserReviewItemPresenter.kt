package com.tomclaw.appsend.screen.details.adapter.user_review

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import java.text.DateFormat
import java.util.Locale

class UserReviewItemPresenter(
    private val dateFormatter: DateFormat,
    private val locale: Locale,
    private val listener: ItemListener,
) : ItemPresenter<UserReviewItemView, UserReviewItem> {

    override fun bindView(view: UserReviewItemView, item: UserReviewItem, position: Int) {
        item.user.icon?.let(view::setMemberIcon)
        view.setMemberBadge(item.user.primaryBadge)
        val name = item.user.name.takeIf { !it.isNullOrBlank() }
            ?: item.user.icon?.label?.get(locale.language)
            ?: item.user.icon?.label?.get(DEFAULT_LOCALE).orEmpty()
        view.setMemberName(name)
        view.setRating(item.score.toFloat())
        val date: String = dateFormatter.format(item.time)
        view.setDate(date)
        view.setReview(item.text)
        view.setOnEditListener { listener.onRateClick(item.score.toFloat(), item.text) }
    }

}
