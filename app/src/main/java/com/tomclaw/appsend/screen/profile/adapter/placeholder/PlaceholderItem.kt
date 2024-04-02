package com.tomclaw.appsend.screen.profile.adapter.placeholder

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaceholderItem(
    override val id: Long,
) : Item, Parcelable
