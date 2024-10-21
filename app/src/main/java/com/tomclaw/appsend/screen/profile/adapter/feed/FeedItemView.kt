package com.tomclaw.appsend.screen.profile.adapter.feed

import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.getAttributedColor
import com.tomclaw.appsend.util.getColor

interface FeedItemView : ItemView {

    fun setFeedCount(count: Int)

    fun setPubsCount(count: Int)

    fun setSubsCount(count: Int)

    fun setOnFeedClickListener(listener: (() -> Unit)?)

    fun setOnSubsClickListener(listener: (() -> Unit)?)

    fun setOnPubsClickListener(listener: (() -> Unit)?)

}

class FeedItemViewHolder(view: View) : BaseViewHolder(view), FeedItemView {

    private val context = view.context
    private val feedBlockView: View = view.findViewById(R.id.feed_block)
    private val subsBlockView: View = view.findViewById(R.id.subs_block)
    private val pubsBlockView: View = view.findViewById(R.id.pubs_block)
    private val feedCountView: TextView = view.findViewById(R.id.feed_count)
    private val subsCountView: TextView = view.findViewById(R.id.subs_count)
    private val pubsCountView: TextView = view.findViewById(R.id.pubs_count)

    private var feedClickListener: (() -> Unit)? = null
    private var subsClickListener: (() -> Unit)? = null
    private var pubsClickListener: (() -> Unit)? = null

    init {
        feedBlockView.setOnClickListener { feedClickListener?.invoke() }
        subsBlockView.setOnClickListener { subsClickListener?.invoke() }
        pubsBlockView.setOnClickListener { pubsClickListener?.invoke() }
    }

    override fun setFeedCount(count: Int) {
        feedCountView.bind(count.toString())
        feedCountView.setTextColor(getAttributedColor(context, getTextColor(count)))
    }

    override fun setSubsCount(count: Int) {
        subsCountView.bind(count.toString())
        subsCountView.setTextColor(getAttributedColor(context, getTextColor(count)))
    }

    override fun setPubsCount(count: Int) {
        pubsCountView.bind(count.toString())
        pubsCountView.setTextColor(getAttributedColor(context, getTextColor(count)))
    }

    override fun setOnFeedClickListener(listener: (() -> Unit)?) {
        this.feedClickListener = listener
    }

    override fun setOnSubsClickListener(listener: (() -> Unit)?) {
        this.subsClickListener = listener
    }

    override fun setOnPubsClickListener(listener: (() -> Unit)?) {
        this.pubsClickListener = listener
    }

    override fun onUnbind() {
        this.feedClickListener = null
        this.subsClickListener = null
        this.pubsClickListener = null
    }

    private fun getTextColor(count: Int) = if (count > 0) {
        getAttributedColor(context, R.attr.text_primary_color)
    } else {
        getColor(R.color.primary_dark_color, context)
    }

}
