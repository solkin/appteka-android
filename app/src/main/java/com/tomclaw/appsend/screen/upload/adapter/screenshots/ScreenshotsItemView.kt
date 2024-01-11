package com.tomclaw.appsend.screen.upload.adapter.screenshots

import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.upload.adapter.screenshots.adapter.Item

interface ScreenshotsItemView : ItemView {

    fun setItems(items: List<Item>)

    fun setOnClickListener(listener: ((Item) -> Unit)?)

}

class ScreenshotsItemViewHolder(
    view: View,
    private val adapter: ScreenshotsAdapter,
) : BaseViewHolder(view), ScreenshotsItemView {

    private val recycler: RecyclerView = view.findViewById(R.id.recycler)

    private val layoutManager: LinearLayoutManager

    private var clickListener: ((Item) -> Unit)? = null

    init {
        val orientation = RecyclerView.HORIZONTAL
        layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        adapter.setOnItemClickListener { clickListener?.invoke(it) }
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM
    }

    override fun setItems(items: List<Item>) {
        adapter.setItems(items)
    }

    override fun setOnClickListener(listener: ((Item) -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}

private const val DURATION_MEDIUM = 300L
