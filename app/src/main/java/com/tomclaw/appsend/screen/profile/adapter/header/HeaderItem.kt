package com.tomclaw.appsend.screen.profile.adapter.header

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class HeaderItem(
    override val id: Long,
    val name: String,
) : Item, Parcelable
