package com.tomclaw.appsend.screen.installed.adapter.app

import android.os.Parcelable
import com.tomclaw.appsend.download.IDLE
import com.tomclaw.appsend.screen.installed.api.UpdateEntity
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
class AppItem(
    override val id: Long,
    val icon: String?,
    val title: String,
    val version: String,
    val size: Long,
    val installTime: Long,
    val updateTime: Long,
    val packageName: String,
    val path: String?,
    val update: UpdateEntity?,
    val otherEdition: UpdateEntity?,
    val isUserApp: Boolean,
    var isNew: Boolean = false,
    /** Download and install progress of [update], as DownloadManager reports it. */
    var updateState: Int = IDLE,
) : Item, Parcelable
