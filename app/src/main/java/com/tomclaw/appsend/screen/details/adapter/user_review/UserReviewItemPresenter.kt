package com.tomclaw.appsend.screen.details.adapter.user_review

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.DetailsResourceProvider
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import java.util.Locale

class UserReviewItemPresenter(
    private val resourceProvider: DetailsResourceProvider,
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
        val date: String = resourceProvider.formatReviewDate(item.time, item.versionName)
        view.setDate(date)
        view.setReview(item.text)
        view.setOnEditListener { listener.onRateClick(item.score.toFloat(), item.text) }
    }

}
