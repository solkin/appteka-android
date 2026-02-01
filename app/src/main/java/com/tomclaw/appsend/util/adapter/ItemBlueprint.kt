package com.tomclaw.appsend.util.adapter

import android.view.ViewGroup

interface ItemBlueprint<V : ItemView, I : Item> {

    val presenter: ItemPresenter<V, I>

    val viewHolderProvider: ViewHolderBuilder.ViewHolderProvider

    fun isRelevantItem(item: Item): Boolean

    fun createViewHolder(parent: ViewGroup): BaseItemViewHolder {
        return viewHolderProvider.createViewHolder(parent)
    }

}
