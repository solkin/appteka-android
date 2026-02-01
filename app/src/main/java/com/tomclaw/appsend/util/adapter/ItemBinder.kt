package com.tomclaw.appsend.util.adapter

import android.view.ViewGroup

class ItemBinder private constructor(
    private val blueprints: List<ItemBlueprint<*, *>>
) {

    fun getItemViewType(item: Item): Int {
        return blueprints.indexOfFirst { it.isRelevantItem(item) }.takeIf { it >= 0 }
            ?: throw IllegalStateException("No blueprint found for item: ${item::class.java.simpleName}")
    }

    fun createViewHolder(parent: ViewGroup, viewType: Int): BaseItemViewHolder {
        return blueprints[viewType].createViewHolder(parent)
    }

    @Suppress("UNCHECKED_CAST")
    fun bind(
        holder: BaseItemViewHolder,
        item: Item,
        position: Int
    ) {
        val blueprint = blueprints.first { it.isRelevantItem(item) } as ItemBlueprint<ItemView, Item>
        blueprint.presenter.bindView(holder as ItemView, item, position)
    }

    class Builder {
        private val blueprints = mutableListOf<ItemBlueprint<*, *>>()

        fun registerItem(blueprint: ItemBlueprint<*, *>): Builder {
            blueprints.add(blueprint)
            return this
        }

        fun build(): ItemBinder {
            return ItemBinder(blueprints.toList())
        }
    }

}
