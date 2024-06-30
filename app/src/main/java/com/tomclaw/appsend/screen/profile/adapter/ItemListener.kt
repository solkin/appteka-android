package com.tomclaw.appsend.screen.profile.adapter

import com.tomclaw.appsend.screen.profile.adapter.app.AppItem

interface ItemListener {

    fun onAppClick(appId: String, title: String?)

    fun onRatingsClick()

    fun onFavoritesClick()

    fun onUploadsClick(userId: Int)

    fun onLoginClick()

    fun onEditName(name: String?)

    fun onNextPage(last: AppItem, param: (List<AppItem>) -> Unit)

}
