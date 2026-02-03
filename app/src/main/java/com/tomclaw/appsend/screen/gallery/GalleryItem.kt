package com.tomclaw.appsend.screen.gallery

import android.net.Uri
import android.os.Parcelable
import androidx.core.net.toUri
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class GalleryItem(
    val uriString: String,
    val width: Int,
    val height: Int
) : Parcelable {

    companion object {
        fun fromUri(uri: Uri, width: Int, height: Int) =
            GalleryItem(uriString = uri.toString(), width, height)
    }
}

fun GalleryItem.uri() = uriString.toUri()
