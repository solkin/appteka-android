package com.tomclaw.appsend.screen.details.adapter.description

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class DescriptionItem(
    override val id: Long,
    val text: String,
    val versionName: String,
    val versionCode: Int,
    val versionsCount: Int,
    val uploadDate: Long,
    val checksum: String,
) : Item, Parcelable
