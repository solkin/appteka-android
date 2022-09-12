package com.tomclaw.appsend.screen.details.adapter.user_rate

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener

class UserRateItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<UserRateItemView, UserRateItem> {

    override fun bindView(view: UserRateItemView, item: UserRateItem, position: Int) {
        view.setRating(0f)
        view.setOnClickListener { listener.onRateClick(item.appId) }
    }

}
