package com.tomclaw.appsend.screen.profile.adapter.app

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppItem(
    override val id: Long,
    val appId: String,
    val icon: String?,
    val title: String,
    val rating: Float,
) : Item, Parcelable
