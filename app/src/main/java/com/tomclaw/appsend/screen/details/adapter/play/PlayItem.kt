package com.tomclaw.appsend.screen.details.adapter.play

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.categories.Category
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlayItem(
    override val id: Long,
    val rating: Float?,
    val downloads: Int,
    val favorites: Int,
    val size: Long,
    val exclusive: Boolean,
    val openSource: Boolean,
    val category: Category?,
    val osVersion: String?,
    val minSdk: Int?,
    val securityStatus: PlaySecurityStatus?,
    val securityScore: Int?,
) : Item, Parcelable

enum class PlaySecurityStatus {
    SCANNING,
    SAFE,
    SUSPICIOUS,
    MALWARE,
    NOT_CHECKED
}
