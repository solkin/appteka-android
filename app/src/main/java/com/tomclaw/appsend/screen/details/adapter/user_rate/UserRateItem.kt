package com.tomclaw.appsend.screen.details.adapter.user_rate

import android.os.Parcelable
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserRateItem(
    override val id: Long,
    val appId: String,
    /** "app.rate" capability snapshot; null when the server did not send one. */
    val rateCapability: Capability? = null,
) : Item, Parcelable
