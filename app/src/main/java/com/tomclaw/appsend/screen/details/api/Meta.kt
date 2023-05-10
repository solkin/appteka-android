package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class Meta(
    @SerializedName("category")
    val category: Category?,
    @SerializedName("whats_new")
    val whatsNew: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("exclusive")
    val exclusive: Boolean?,
    @SerializedName("open_source")
    val openSource: Boolean?,
    @SerializedName("source_url")
    val sourceUrl: String?,
    @SerializedName("similar")
    val similar: Boolean?,
    @SerializedName("time")
    val time: Long?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("user_icon")
    val userIcon: UserIcon?,
    @SerializedName("rate_count")
    val rateCount: Int?,
    @SerializedName("rating")
    val rating: Float?,
    @SerializedName("scores")
    val scores: Scores?,
) : Parcelable
