package com.tomclaw.appsend.util.adapter

import androidx.recyclerview.widget.DiffUtil

class ItemDiffCallback(
    private val oldItems: List<Item>,
    private val newItems: List<Item>,
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItems[oldItemPosition].id == newItems[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItems[oldItemPosition] == newItems[newItemPosition]

}
