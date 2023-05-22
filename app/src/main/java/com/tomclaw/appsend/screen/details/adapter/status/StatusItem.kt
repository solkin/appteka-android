package com.tomclaw.appsend.screen.details.adapter.status

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatusItem(
    override val id: Long,
    val type: StatusType,
) : Item, Parcelable

enum class StatusType {
    INFO,
    WARNING,
    ERROR
}
