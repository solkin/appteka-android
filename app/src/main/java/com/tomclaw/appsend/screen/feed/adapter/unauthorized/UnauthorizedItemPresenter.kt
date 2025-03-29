package com.tomclaw.appsend.screen.feed.adapter.unauthorized

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.feed.adapter.ItemListener

class UnauthorizedItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<UnauthorizedItemView, UnauthorizedItem> {

    override fun bindView(view: UnauthorizedItemView, item: UnauthorizedItem, position: Int) {
        view.setOnLoginButtonClickListener { listener.onLoginClick() }
    }

}
