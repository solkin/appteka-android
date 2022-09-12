package com.tomclaw.appsend.screen.details.adapter.user_rate

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserRateItem(
    override val id: Long,
    val appId: String,
) : Item, Parcelable
