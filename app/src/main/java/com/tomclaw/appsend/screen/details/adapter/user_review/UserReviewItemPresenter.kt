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
        view.setMemberIcon(item.userIcon)
        val name = item.userName.takeIf { !it.isNullOrBlank() }
            ?: item.userIcon.label[locale.language]
            ?: item.userIcon.label[DEFAULT_LOCALE].orEmpty()
        view.setMemberName(name)
        view.setRating(item.score.toFloat())
        val date: String = dateFormatter.format(item.time)
        view.setDate(date)
        view.setReview(item.text)
        view.setOnEditListener { listener.onRateClick(item.score.toFloat(), item.text) }
    }

}
