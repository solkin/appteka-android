package com.tomclaw.appsend.screen.upload.adapter.selected_app

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.LocalAppEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedAppItem(
    override val id: Long,
    val appEntity: LocalAppEntity
) : Item, Parcelable
