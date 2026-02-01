package com.tomclaw.appsend.screen.profile.adapter.unauthorized

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class UnauthorizedItem(
    override val id: Long,
) : Item, Parcelable
