package com.tomclaw.appsend.screen.profile.adapter.uploads

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadsItem(
    override val id: Long,
    val userId: Int,
    val uploads: Int,
    val downloads: Int,
    val items: List<AppItem>,
) : Item, Parcelable
