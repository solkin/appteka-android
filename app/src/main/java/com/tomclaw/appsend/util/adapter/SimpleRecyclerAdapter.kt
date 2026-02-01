package com.tomclaw.appsend.util.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SimpleRecyclerAdapter(
    private val adapterPresenter: AdapterPresenter,
    private val binder: ItemBinder
) : RecyclerView.Adapter<BaseItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseItemViewHolder {
        return binder.createViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseItemViewHolder, position: Int) {
        val item = adapterPresenter.getItem(position)
        binder.bind(holder, item, position)
    }

    override fun onViewRecycled(holder: BaseItemViewHolder) {
        holder.onUnbind()
        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = adapterPresenter.getItemCount()

    override fun getItemId(position: Int): Long = adapterPresenter.getItemId(position)

    override fun getItemViewType(position: Int): Int = adapterPresenter.getItemViewType(position)

}
