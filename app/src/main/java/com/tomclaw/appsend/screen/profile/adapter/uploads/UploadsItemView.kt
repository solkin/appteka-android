package com.tomclaw.appsend.screen.profile.adapter.uploads

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface UploadsItemView : ItemView {

    fun setUploadsCount(count: String)

    fun setDownloadsCount(count: String)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setOnNextPageListener(listener: (() -> Unit)?)

    fun notifyChanged()

}

class UploadsItemViewHolder(
    view: View,
    private val adapter: SimpleRecyclerAdapter,
) : BaseViewHolder(view), UploadsItemView {

    private val context = view.context
    private val uploadsBlock: View = view.findViewById(R.id.uploads_block)
    private val uploadsCountText: TextView = view.findViewById(R.id.uploads_count)
    private val subtitleText: TextView = view.findViewById(R.id.subtitle)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)

    private val layoutManager: LinearLayoutManager

    private var clickListener: (() -> Unit)? = null
    private var nextPageListener: (() -> Unit)? = null

    init {
        val orientation = RecyclerView.HORIZONTAL
        layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM
        recycler.addRecyclerListener { holder ->
            holder.itemId
        }
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recycler: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recycler, newState)
                if (!recycler.canScrollHorizontally(1) && newState == SCROLL_STATE_IDLE) {
                    nextPageListener?.invoke()
                }
            }
        }
        recycler.addOnScrollListener(scrollListener)

        uploadsBlock.setOnClickListener { clickListener?.invoke() }
    }

    override fun setUploadsCount(count: String) {
        uploadsCountText.bind(count)
    }

    override fun setDownloadsCount(count: String) {
        subtitleText.bind(context.getString(R.string.total_downloads_count, count))
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setOnNextPageListener(listener: (() -> Unit)?) {
        this.nextPageListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyChanged() {
        adapter.notifyDataSetChanged()
    }

    override fun onUnbind() {
        this.clickListener = null
        this.nextPageListener = null
    }

}

private const val DURATION_MEDIUM = 300L
