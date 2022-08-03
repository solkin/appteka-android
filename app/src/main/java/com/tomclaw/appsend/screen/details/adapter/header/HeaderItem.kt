package com.tomclaw.appsend.screen.details.adapter.header

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.DownloadState
import kotlinx.parcelize.Parcelize

@Parcelize
data class HeaderItem(
    override val id: Long,
    val icon: String?,
    val packageName: String,
    val label: String,
    val userId: Int?,
    val userIcon: UserIcon?,
    val userName: String?,
    val downloadState: DownloadState?
) : Item, Parcelable
