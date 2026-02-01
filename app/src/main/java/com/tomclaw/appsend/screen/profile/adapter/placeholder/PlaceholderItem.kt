package com.tomclaw.appsend.screen.profile.adapter.placeholder

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaceholderItem(
    override val id: Long,
) : Item, Parcelable
