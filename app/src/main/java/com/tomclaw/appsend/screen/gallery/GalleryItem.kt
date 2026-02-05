package com.tomclaw.appsend.screen.gallery

import android.net.Uri
import androidx.core.net.toUri

data class GalleryItem(
    val uri: Uri,
    val width: Int,
    val height: Int
) {

    fun serialize(): String = "$uri$DELIMITER$width$DELIMITER$height"

    companion object {
        private const val DELIMITER = "\u001F"
        private const val ITEM_DELIMITER = "\u001E"

        fun deserialize(data: String): GalleryItem? = runCatching {
            val parts = data.split(DELIMITER)
            GalleryItem(parts[0].toUri(), parts[1].toInt(), parts[2].toInt())
        }.getOrNull()

        fun serializeList(items: List<GalleryItem>): String =
            items.joinToString(ITEM_DELIMITER) { it.serialize() }

        fun deserializeList(data: String?): List<GalleryItem>? =
            data?.split(ITEM_DELIMITER)
                ?.mapNotNull { deserialize(it) }
                ?.takeIf { it.isNotEmpty() }
    }
}
