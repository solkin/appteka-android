package com.tomclaw.appsend.screen.upload.adapter.screen_append

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenAppendItem(
    override val id: Long,
) : Item, Parcelable
