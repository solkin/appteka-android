package com.tomclaw.appsend.screen.details.adapter

interface ItemListener {

    fun onProfileClick(userId: Int)

    fun onPermissionsClick(permissions: List<String>)

    fun onScoresClick()

    fun onInstallClick()

    fun onLaunchClick(packageName: String)

    fun onRemoveClick(packageName: String)

    fun onCancelClick(appId: String)

    fun onRateClick(rating: Float, review: String?)

}
