package com.tomclaw.appsend.screen.upload.adapter

interface ItemListener {

    fun onSelectAppClick()

    fun onDiscardClick()

    fun onNoticeClick()

    fun onCategoryClick()

    fun onWhatsNewChanged(text: String)

}
