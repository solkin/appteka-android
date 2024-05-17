package com.tomclaw.appsend.screen.details.adapter

import com.tomclaw.appsend.screen.details.adapter.screenshot.ScreenshotItem
import com.tomclaw.appsend.screen.details.adapter.status.StatusAction

interface ItemListener {

    fun onProfileClick(userId: Int)

    fun onPermissionsClick(permissions: List<String>)

    fun onScoresClick()

    fun onInstallClick()

    fun onLaunchClick(packageName: String)

    fun onRemoveClick(packageName: String)

    fun onCancelClick(appId: String)

    fun onDiscussClick()

    fun onTranslateClick()

    fun onGooglePlayClick()

    fun onRateClick(rating: Float, review: String?)

    fun onVersionsClick()

    fun onStatusAction(type: StatusAction)

    fun onScreenshotClick(items: List<ScreenshotItem>, clicked: Int)

}
