package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.dto.UserMark
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
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
    @SerializedName("screenshots")
    val screenshots: List<Screenshot>?,
    @SerializedName("time")
    val time: Long?,
    @SerializedName("author")
    val author: UserMark? = null,
    @SerializedName("rate_count")
    val rateCount: Int?,
    @SerializedName("rating")
    val rating: Float?,
    @SerializedName("scores")
    val scores: Scores?,
    @SerializedName("ai_tags")
    val aiTags: List<String>? = null,
    @SerializedName("ai_note")
    val aiNote: String? = null,
    @SerializedName("ai_short_description")
    val aiShortDescription: String? = null,
    @SerializedName("ai_status")
    val aiStatus: String? = null,
    // What this app was found to contain. The app page is never hidden
    // by a viewer's filter — reaching it takes a deliberate tap — so
    // this is what lets us label it instead.
    @SerializedName("content_flags")
    val contentFlags: List<String>? = null,
) : Parcelable
