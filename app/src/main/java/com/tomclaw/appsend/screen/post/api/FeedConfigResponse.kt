package com.tomclaw.appsend.screen.post.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.screen.feed.api.Reaction
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedConfigResponse(
    @SerializedName("post_max_length")
    val postMaxLength: Int,
    @SerializedName("post_max_images")
    val postMaxImages: Int,
    @SerializedName("reactions")
    val reactions: List<Reaction>?,
) : Parcelable
