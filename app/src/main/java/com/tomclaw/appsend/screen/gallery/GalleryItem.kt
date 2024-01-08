package com.tomclaw.appsend.screen.gallery

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryItem(
    val uri: Uri,
    val width: Int,
    val height: Int
) : Parcelable
