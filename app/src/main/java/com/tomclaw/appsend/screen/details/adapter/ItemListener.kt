package com.tomclaw.appsend.screen.details.adapter

interface ItemListener {

    fun onProfileClick(userId: Int)

    fun onPermissionsClick(permissions: List<String>)

    fun onScoresClick()

    fun onInstallClick()

    fun onOpenClick(packageName: String)

    fun onRemoveClick(packageName: String)

}
