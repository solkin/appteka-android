package com.tomclaw.appsend.screen.post

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.post.dto.PostScreenshot

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
        TODO("Not yet implemented")
    }

}
