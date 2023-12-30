package com.tomclaw.appsend.screen.gallery

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryItem(
    val url: String,
    val width: Int,
    val height: Int
) : Parcelable