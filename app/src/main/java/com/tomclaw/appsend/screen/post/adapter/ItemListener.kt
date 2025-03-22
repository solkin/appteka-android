package com.tomclaw.appsend.screen.post.adapter

import com.tomclaw.appsend.screen.post.adapter.image.ImageItem

interface ItemListener {

    fun onTextChanged(text: String)

    fun onSubmitClick()

    fun onScreenAppendClick()

    fun onImageClick(item: ImageItem)

    fun onImageDelete(item: ImageItem)

}
