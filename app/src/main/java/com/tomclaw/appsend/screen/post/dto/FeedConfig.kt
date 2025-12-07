package com.tomclaw.appsend.screen.post.dto

import android.os.Parcelable
import com.tomclaw.appsend.screen.feed.api.Reaction
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedConfig(
    val postMaxLength: Int,
    val postMaxImages: Int,
    val reactions: List<Reaction>?,
) : Parcelable
