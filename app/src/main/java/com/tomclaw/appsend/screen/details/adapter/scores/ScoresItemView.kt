package com.tomclaw.appsend.screen.details.adapter.scores

import android.view.View
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.details.api.RATING_SCOPE_BUILD
import com.tomclaw.appsend.screen.details.api.RATING_SCOPE_EDITION
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface ScoresItemView : ItemView {

    fun showScopeToggle(selectedScope: String)

    fun hideScopeToggle()

    fun setOnScopeSelectedListener(listener: ((String) -> Unit)?)

    fun showScoresSummary()

    fun showEmptyState()

    fun setRating(rating: Float)

    fun setScores(totalCount: Int, five: Int, four: Int, three: Int, two: Int, one: Int)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ScoresItemViewHolder(view: View) : BaseItemViewHolder(view), ScoresItemView {

    private val scopeGroup: MaterialButtonToggleGroup = view.findViewById(R.id.reviews_scope_group)
    private val summaryContainer: View = view.findViewById(R.id.scores_summary_container)
    private val emptyView: TextView = view.findViewById(R.id.scores_empty_view)
    private val ratingScore: TextView = view.findViewById(R.id.rating_score)
    private val ratingIndicator: RatingBar = view.findViewById(R.id.small_rating_indicator)
    private val ratedCount: TextView = view.findViewById(R.id.rates_count)
    private val fiveElement: ProgressBar = view.findViewById(R.id.rating_detail_element_five)
    private val fourElement: ProgressBar = view.findViewById(R.id.rating_detail_element_four)
    private val threeElement: ProgressBar = view.findViewById(R.id.rating_detail_element_three)
    private val twoElement: ProgressBar = view.findViewById(R.id.rating_detail_element_two)
    private val oneElement: ProgressBar = view.findViewById(R.id.rating_detail_element_one)

    private var clickListener: (() -> Unit)? = null
    private var scopeSelectedListener: ((String) -> Unit)? = null
    private var suppressScopeChange: Boolean = false

    init {
        summaryContainer.setOnClickListener { clickListener?.invoke() }
        scopeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (suppressScopeChange || !isChecked) return@addOnButtonCheckedListener
            val scope = when (checkedId) {
                R.id.scope_build -> RATING_SCOPE_BUILD
                else -> RATING_SCOPE_EDITION
            }
            scopeSelectedListener?.invoke(scope)
        }
    }

    override fun showScopeToggle(selectedScope: String) {
        scopeGroup.show()
        suppressScopeChange = true
        val targetId = if (selectedScope == RATING_SCOPE_BUILD) {
            R.id.scope_build
        } else {
            R.id.scope_edition
        }
        if (scopeGroup.checkedButtonId != targetId) {
            scopeGroup.check(targetId)
        }
        suppressScopeChange = false
    }

    override fun hideScopeToggle() {
        scopeGroup.hide()
    }

    override fun setOnScopeSelectedListener(listener: ((String) -> Unit)?) {
        this.scopeSelectedListener = listener
    }

    override fun showScoresSummary() {
        summaryContainer.show()
        emptyView.hide()
    }

    override fun showEmptyState() {
        summaryContainer.hide()
        emptyView.show()
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
        summaryContainer.isClickable = listener != null
    }

    override fun onUnbind() {
        this.clickListener = null
        this.scopeSelectedListener = null
    }

}
