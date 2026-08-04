package com.tomclaw.appsend.screen.details.adapter.content_flags

import com.tomclaw.appsend.util.adapter.ItemPresenter

class ContentFlagsItemPresenter : ItemPresenter<ContentFlagsItemView, ContentFlagsItem> {

    override fun bindView(view: ContentFlagsItemView, item: ContentFlagsItem, position: Int) {
        view.showFlags(item.codes)
    }

}
