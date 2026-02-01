package com.tomclaw.appsend.screen.details.adapter.scores

import android.view.View
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface ScoresItemView : ItemView {

    fun setRating(rating: Float)

    fun setScores(totalCount: Int, five: Int, four: Int, three: Int, two: Int, one: Int)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ScoresItemViewHolder(view: View) : BaseItemViewHolder(view), ScoresItemView {

    private val ratingScore: TextView = view.findViewById(R.id.rating_score)
    private val ratingIndicator: RatingBar = view.findViewById(R.id.small_rating_indicator)
    private val ratedCount: TextView = view.findViewById(R.id.rates_count)
    private val fiveElement: ProgressBar = view.findViewById(R.id.rating_detail_element_five)
    private val fourElement: ProgressBar = view.findViewById(R.id.rating_detail_element_four)
    private val threeElement: ProgressBar = view.findViewById(R.id.rating_detail_element_three)
    private val twoElement: ProgressBar = view.findViewById(R.id.rating_detail_element_two)
    private val oneElement: ProgressBar = view.findViewById(R.id.rating_detail_element_one)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setRating(rating: Float) {
        ratingScore.bind(rating.toString())
        ratingIndicator.rating = rating
    }

    override fun setScores(
        totalCount: Int,
        five: Int,
        four: Int,
        three: Int,
        two: Int,
        one: Int,
    ) {
        ratedCount.bind(totalCount.toString())
        fiveElement.progress = five
        fourElement.progress = four
        threeElement.progress = three
        twoElement.progress = two
        oneElement.progress = one
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
