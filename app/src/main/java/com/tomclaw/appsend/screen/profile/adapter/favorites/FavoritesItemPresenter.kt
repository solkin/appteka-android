package com.tomclaw.appsend.screen.profile.adapter.favorites

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.profile.adapter.ItemListener

class FavoritesItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<FavoritesItemView, FavoritesItem> {

    override fun bindView(view: FavoritesItemView, item: FavoritesItem, position: Int) {
        view.setCount(item.count)
        view.setOnClickListener { listener.onFavoritesClick() }
    }

}
