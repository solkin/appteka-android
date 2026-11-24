package com.tomclaw.appsend.screen.ratings.adapter.rating

import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.ratings.RatingsResourceProvider
import com.tomclaw.appsend.screen.ratings.adapter.ItemListener
import com.tomclaw.appsend.util.adapter.ItemPresenter
import java.util.Locale

class RatingItemPresenter(
    private val resourceProvider: RatingsResourceProvider,
    private val locale: Locale,
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

        item.user.icon?.let(view::setUserIcon)
        view.setUserBadge(item.user.primaryBadge)
        val name = item.user.name.takeIf { !it.isNullOrBlank() }
            ?: item.user.icon?.label?.get(locale.language)
            ?: item.user.icon?.label?.get(DEFAULT_LOCALE).orEmpty()
        view.setUserName(name)
        view.setRating(item.score.toFloat())
        val date: String = resourceProvider.formatReviewDate(item.time, item.versionName)
        view.setDate(date)
        view.setComment(item.text)
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (item.showRatingMenu) {
            view.showRatingMenu()
        } else {
            view.hideRatingMenu()
        }
        view.setOnRatingClickListener { listener.onItemClick(item) }
        view.setOnDeleteClickListener { listener.onDeleteClick(item) }
    }

}
