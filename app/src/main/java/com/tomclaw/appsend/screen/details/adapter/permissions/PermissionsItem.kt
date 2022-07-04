package com.tomclaw.appsend.screen.details.adapter.permissions

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class PermissionsItem(
    override val id: Long,
    val permissions: List<String>?,
) : Item, Parcelable
