package com.tomclaw.appsend.screen.profile.adapter.moderation

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.ItemListener

class ModerationItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ModerationItemView, ModerationItem> {

    override fun bindView(view: ModerationItemView, item: ModerationItem, position: Int) {
        if (item.count > 0) {
            view.setCount(item.count)
            view.showIndicator()
        } else {
            view.setNoApps()
            view.hideIndicator()
        }
        view.setOnClickListener { listener.onModerationClick() }
    }

}

