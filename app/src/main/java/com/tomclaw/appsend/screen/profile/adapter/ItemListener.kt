package com.tomclaw.appsend.screen.profile.adapter

import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import com.tomclaw.appsend.screen.profile.adapter.review.ReviewItem

interface ItemListener {

    fun onAppClick(item: AppItem)

    fun onRatingClick(item: ReviewItem)

    fun onFavoritesClick()

    fun onUploadsClick(userId: Int)

    fun onNextPage(last: AppItem, param: (List<AppItem>) -> Unit)

}
