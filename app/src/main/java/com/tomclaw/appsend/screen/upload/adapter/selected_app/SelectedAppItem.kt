package com.tomclaw.appsend.screen.upload.adapter.selected_app

import android.content.pm.PackageInfo
import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedAppItem(
    override val id: Long,
    val packageInfo: PackageInfo
) : Item, Parcelable
