package com.tomclaw.appsend.screen.post

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.post.dto.PostScreenshot
import com.tomclaw.appsend.screen.upload.adapter.screen_append.ScreenAppendItem
import com.tomclaw.appsend.screen.upload.adapter.screen_image.ScreenImageItem
import com.tomclaw.appsend.screen.upload.adapter.screenshots.ScreenshotsItem

interface PostConverter {

    fun convert(
        screenshots: List<PostScreenshot>,
        test: String,
    ): List<Item>

}

class PostConverterImpl() : PostConverter {

    override fun convert(
        screenshots: List<PostScreenshot>,
        test: String
    ): List<Item> {
        var id: Long = 1
        val items = ArrayList<Item>()

        items += ScreenshotsItem(
            id = id++,
            items = screenshots.map {
                ScreenImageItem(
                    id = it.longId(),
                    original = it.original,
                    preview = it.preview,
                    width = it.width,
                    height = it.height,
                    remote = it.remote()
                )
            } + ScreenAppendItem(id++)
        )

        return items
    }

    private fun PostScreenshot.longId(): Long =
        (scrId?.hashCode() ?: original.toString().hashCode()).toLong()

    private fun PostScreenshot.remote(): Boolean = scrId != null

}
