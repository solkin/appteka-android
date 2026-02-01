package com.tomclaw.appsend.screen.profile.adapter.reviews

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface ReviewsItemView : ItemView {

    fun setRatingsCount(count: String)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun notifyChanged()

}

class ReviewsItemViewHolder(
    view: View,
    private val adapter: SimpleRecyclerAdapter,
) : BaseItemViewHolder(view), ReviewsItemView {

    private val ratingsBlock: View = view.findViewById(R.id.ratings_block)
    private val ratingsCountText: TextView = view.findViewById(R.id.ratings_count)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val ratingsButton: View = view.findViewById(R.id.show_ratings_button)

    private val layoutManager: LinearLayoutManager

    private var clickListener: (() -> Unit)? = null

    init {
        val orientation = RecyclerView.VERTICAL
        layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        ratingsBlock.setOnClickListener { clickListener?.invoke() }
        ratingsButton.setOnClickListener { clickListener?.invoke() }
    }

    override fun setRatingsCount(count: String) {
        ratingsCountText.bind(count)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun notifyChanged() {
        adapter.notifyDataSetChanged()
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}

private const val DURATION_MEDIUM = 300L
