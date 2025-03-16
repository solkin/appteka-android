package com.tomclaw.appsend.screen.post.adapter

import com.tomclaw.appsend.screen.post.adapter.screen_image.ScreenImageItem

interface ItemListener {

    fun onTextChanged(text: String)

    fun onSubmitClick()

    fun onScreenAppendClick()

    fun onScreenshotClick(item: ScreenImageItem)

    fun onScreenshotDelete(item: ScreenImageItem)

}
