package com.tomclaw.appsend.screen.post.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedPostResponse(
    @SerializedName("id")
    val postId: Int
) : Parcelable
