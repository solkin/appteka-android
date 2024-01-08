package com.tomclaw.appsend.screen.gallery.adapter.image

import android.net.Uri
import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageItem(
    override val id: Long,
    val uri: Uri,
): Parcelable, Item
