package com.tomclaw.appsend.screen.upload.adapter

import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem

interface ItemListener {

    fun onSelectAppClick()

    fun onDiscardClick()

    fun onNoticeClick()

    fun onCategoryClick()

    fun onWhatsNewChanged(text: String)

    fun onDescriptionChanged(text: String)

    fun onExclusiveChanged(value: Boolean)

    fun onOpenSourceChanged(value: Boolean, url: String)

    fun onSubmitClick()

    fun onOtherVersionsClick(items: List<VersionItem>)

}
