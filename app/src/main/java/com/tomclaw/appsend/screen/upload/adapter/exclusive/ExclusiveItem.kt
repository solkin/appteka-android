package com.tomclaw.appsend.screen.upload.adapter.exclusive

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExclusiveItem(
    override val id: Long,
    val value: Boolean,
) : Item, Parcelable
