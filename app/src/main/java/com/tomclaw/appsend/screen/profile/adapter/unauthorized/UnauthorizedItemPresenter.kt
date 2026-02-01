package com.tomclaw.appsend.screen.profile.adapter.unauthorized

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.ItemListener

class UnauthorizedItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<UnauthorizedItemView, UnauthorizedItem> {

    override fun bindView(view: UnauthorizedItemView, item: UnauthorizedItem, position: Int) {
        view.setOnLoginButtonClickListener { listener.onLoginClick() }
    }

}
