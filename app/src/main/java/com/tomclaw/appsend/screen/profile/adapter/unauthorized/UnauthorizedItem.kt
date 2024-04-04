package com.tomclaw.appsend.screen.profile.adapter.unauthorized

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class UnauthorizedItem(
    override val id: Long,
) : Item, Parcelable
