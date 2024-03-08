package com.tomclaw.appsend.screen.profile.adapter.rating

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingItem(
    override val id: Long,
    val appId: String,
    val icon: String?,
    val title: String,
    val version: String,
    val rating: Float,
    val text: String?,
    val time: Long,
) : Item, Parcelable
