package com.tomclaw.appsend.screen.post.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
data class FeedPostResponse(
    @SerializedName("id")
    val postId: Int
) : Parcelable
