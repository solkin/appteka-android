package com.tomclaw.appsend.screen.post

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.post.adapter.append.AppendItem
import com.tomclaw.appsend.screen.post.adapter.image.ImageItem
import com.tomclaw.appsend.screen.post.adapter.ribbon.RibbonItem
import com.tomclaw.appsend.screen.post.adapter.submit.SubmitItem
import com.tomclaw.appsend.screen.post.adapter.text.TextItem
import com.tomclaw.appsend.screen.post.dto.FeedConfig
import com.tomclaw.appsend.screen.post.dto.PostImage
import com.tomclaw.appsend.util.trim
import kotlin.math.min

interface PostConverter {

    fun convert(
        images: List<PostImage>,
        text: String,
        highlightErrors: Boolean,
        config: FeedConfig,
    ): List<Item>

}

class PostConverterImpl() : PostConverter {

    override fun convert(
        images: List<PostImage>,
        text: String,
        highlightErrors: Boolean,
        config: FeedConfig,
    ): List<Item> {
        var id: Long = 1
        val items = ArrayList<Item>()

        items += TextItem(
            id++,
            text = text.substring(0, min(text.length, config.postMaxLength)),
            errorRequiredField = highlightErrors && text.isBlank(),
            maxLength = config.postMaxLength,
        )

        items += RibbonItem(
            id = id++,
            items = images
                .map {
                    ImageItem(
                        id = it.longId(),
                        original = it.original,
                        preview = it.preview,
                        width = it.width,
                        height = it.height,
                        remote = it.remote()
                    )
                }
                .plus(AppendItem(id++))
                .trim(config.postMaxImages)
        )

        items += SubmitItem(id++)

        return items
    }

    private fun PostImage.longId(): Long =
        (scrId?.hashCode() ?: original.toString().hashCode()).toLong()

    private fun PostImage.remote(): Boolean = scrId != null

}
