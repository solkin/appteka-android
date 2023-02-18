package com.tomclaw.appsend.screen.upload.adapter.select_app

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectAppItem(
    override val id: Long
) : Item, Parcelable
