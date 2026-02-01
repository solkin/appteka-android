package com.tomclaw.appsend.screen.profile.adapter.favorites

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class FavoritesItem(
    override val id: Long,
    val count: Int,
) : Item, Parcelable
