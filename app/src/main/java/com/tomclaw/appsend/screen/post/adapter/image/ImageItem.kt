package com.tomclaw.appsend.screen.post.adapter.image

import android.net.Uri
import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageItem(
    override val id: Long,
    val original: Uri,
    val preview: Uri,
    val width: Int,
    val height: Int,
    val remote: Boolean,
) : Item, Parcelable
