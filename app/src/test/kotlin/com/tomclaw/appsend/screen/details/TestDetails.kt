package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.dto.UserMark
import com.tomclaw.appsend.screen.details.api.AppEdition
import com.tomclaw.appsend.screen.details.api.AppInfo
import com.tomclaw.appsend.screen.details.api.AppVersion
import com.tomclaw.appsend.screen.details.api.Details

/** Details of a build with [versionCode], with only what a test names filled in. */
fun testDetails(
    versionCode: Int = 1,
    editions: List<AppEdition>? = null,
    flat: List<AppVersion> = emptyList(),
) = Details(
    url = "",
    topicId = null,
    msgCount = null,
    actions = null,
    ratingsList = null,
    userRating = null,
    versions = ArrayList(flat),
    editions = editions,
    link = "",
    expiresIn = 0,
    meta = null,
    info = AppInfo(
        appId = "id$versionCode",
        label = null,
        labels = null,
        icon = null,
        fileStatus = 0,
        packageName = "app",
        version = "$versionCode",
        versionCode = versionCode,
        sdkVersion = 21,
        androidVersion = "",
        permissions = null,
        size = 0,
        downloads = null,
        downloadTime = null,
        favorites = null,
        time = 0,
        sha1 = "",
        author = UserMark(id = 1),
        abi = null,
        languages = null,
    ),
    isFavorite = null,
    security = null,
    translation = null,
    developer = null,
    moderation = null,
)
