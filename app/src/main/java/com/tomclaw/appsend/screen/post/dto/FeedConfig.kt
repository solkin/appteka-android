package com.tomclaw.appsend.screen.post.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedConfig(
    val postMaxLength: Int,
    val postMaxImages: Int,
) : Parcelable
