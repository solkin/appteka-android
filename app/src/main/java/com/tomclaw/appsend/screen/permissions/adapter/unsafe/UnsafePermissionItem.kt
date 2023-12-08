package com.tomclaw.appsend.screen.permissions.adapter.unsafe

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class UnsafePermissionItem(
    override val id: Long,
    val description: String?,
    val permission: String,
) : Item, Parcelable
