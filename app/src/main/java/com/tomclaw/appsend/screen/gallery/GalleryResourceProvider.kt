package com.tomclaw.appsend.screen.gallery

import android.content.res.Resources
import com.tomclaw.appsend.R
import java.util.Locale

interface GalleryResourceProvider {

    fun formatTitle(current: Int, total: Int): String

}

class GalleryResourceProviderImpl(
    val resources: Resources,
    val locale: Locale,
) : GalleryResourceProvider {

    override fun formatTitle(current: Int, total: Int): String {
        return resources.getString(R.string.gallery_title, current, total)
    }

}
