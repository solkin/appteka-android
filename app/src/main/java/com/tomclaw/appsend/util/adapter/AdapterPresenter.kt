package com.tomclaw.appsend.util.adapter

interface AdapterPresenter {

    fun onDataSourceChanged(items: List<Item>)

    fun getItemCount(): Int

    fun getItem(position: Int): Item

    fun getItemId(position: Int): Long

    fun getItemViewType(position: Int): Int

}

class SimpleAdapterPresenter(
    private val binder: ItemBinder
) : AdapterPresenter {

    private val items = mutableListOf<Item>()

    override fun onDataSourceChanged(items: List<Item>) {
        this.items.clear()
        this.items.addAll(items)
    }

    override fun getItemCount(): Int = items.size

    override fun getItem(position: Int): Item = items[position]

    override fun getItemId(position: Int): Long = items[position].id

    override fun getItemViewType(position: Int): Int {
        return binder.getItemViewType(items[position])
    }

}
