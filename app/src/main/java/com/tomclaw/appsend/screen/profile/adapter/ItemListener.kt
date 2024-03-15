package com.tomclaw.appsend.screen.profile.adapter

import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import com.tomclaw.appsend.screen.profile.adapter.rating.RatingItem

interface ItemListener {

    fun onAppClick(item: AppItem)

    fun onRatingClick(item: RatingItem)

    fun onFavoritesClick()

    fun onUploadsClick(userId: Int)

    fun onNextPage(last: AppItem)

}
