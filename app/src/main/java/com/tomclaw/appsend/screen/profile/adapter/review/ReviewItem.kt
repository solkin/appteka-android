package com.tomclaw.appsend.screen.profile.adapter.review

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewItem(
    override val id: Long,
    val appId: String,
    val rateId: Int,
    val icon: String?,
    val title: String,
    val version: String,
    val rating: Float,
    val text: String?,
    val time: Long,
) : Item, Parcelable
