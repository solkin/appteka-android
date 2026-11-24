package com.tomclaw.appsend.screen.details.adapter.scores

import android.view.View
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface ScoresItemView : ItemView {

    fun showScopeEdition()

    fun showScopeBuild()

    fun hideScopeToggle()

    fun setBuildScopeVersion(versionName: String)

    fun setOnEditionScopeClickListener(listener: (() -> Unit)?)

    fun setOnBuildScopeClickListener(listener: (() -> Unit)?)

    fun showScoresSummary()

    fun showEmptyState()

    fun setOnShowAllVersionsClickListener(listener: (() -> Unit)?)

    fun showReviewsProgress()

    fun showReviewsError()

    fun hideReviewsState()

    fun setOnRetryClickListener(listener: (() -> Unit)?)

    fun setRating(rating: Float)

    fun setScores(totalCount: Int, five: Int, four: Int, three: Int, two: Int, one: Int)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class ScoresItemViewHolder(view: View) : BaseItemViewHolder(view), ScoresItemView {

    private val scopeGroup: MaterialButtonToggleGroup = view.findViewById(R.id.reviews_scope_group)
    private val scopeEditionButton: View = view.findViewById(R.id.scope_edition)
    private val scopeBuildButton: TextView = view.findViewById(R.id.scope_build)
    private val summaryContainer: View = view.findViewById(R.id.scores_summary_container)
    private val emptyContainer: View = view.findViewById(R.id.scores_empty_container)
    private val showAllVersionsButton: View = view.findViewById(R.id.show_all_versions_button)
    private val reviewsProgress: View = view.findViewById(R.id.reviews_progress)
    private val reviewsErrorContainer: View = view.findViewById(R.id.reviews_error_container)
    private val reviewsRetryButton: View = view.findViewById(R.id.reviews_retry_button)
    private val ratingScore: TextView = view.findViewById(R.id.rating_score)
    private val ratingIndicator: RatingBar = view.findViewById(R.id.small_rating_indicator)
    private val ratedCount: TextView = view.findViewById(R.id.rates_count)
    private val fiveElement: ProgressBar = view.findViewById(R.id.rating_detail_element_five)
    private val fourElement: ProgressBar = view.findViewById(R.id.rating_detail_element_four)
    private val threeElement: ProgressBar = view.findViewById(R.id.rating_detail_element_three)
    private val twoElement: ProgressBar = view.findViewById(R.id.rating_detail_element_two)
    private val oneElement: ProgressBar = view.findViewById(R.id.rating_detail_element_one)

    private var clickListener: (() -> Unit)? = null
    private var editionScopeClickListener: (() -> Unit)? = null
    private var buildScopeClickListener: (() -> Unit)? = null
    private var showAllVersionsClickListener: (() -> Unit)? = null
    private var retryClickListener: (() -> Unit)? = null

    init {
        summaryContainer.setOnClickListener { clickListener?.invoke() }
        // Clicks, not the checked listener: re-checking on bind must not emit a selection.
        scopeEditionButton.setOnClickListener { editionScopeClickListener?.invoke() }
        scopeBuildButton.setOnClickListener { buildScopeClickListener?.invoke() }
        showAllVersionsButton.setOnClickListener { showAllVersionsClickListener?.invoke() }
        reviewsRetryButton.setOnClickListener { retryClickListener?.invoke() }
    }

    override fun showScopeEdition() {
        scopeGroup.show()
        scopeGroup.check(R.id.scope_edition)
    }

    override fun showScopeBuild() {
        scopeGroup.show()
        scopeGroup.check(R.id.scope_build)
    }

    override fun hideScopeToggle() {
        scopeGroup.hide()
    }

    override fun setBuildScopeVersion(versionName: String) {
        scopeBuildButton.text = scopeBuildButton.resources.getString(
            R.string.reviews_scope_build,
            versionName,
        )
    }

    override fun setOnEditionScopeClickListener(listener: (() -> Unit)?) {
        this.editionScopeClickListener = listener
    }

    override fun setOnBuildScopeClickListener(listener: (() -> Unit)?) {
        this.buildScopeClickListener = listener
    }

    override fun showScoresSummary() {
        summaryContainer.show()
        emptyContainer.hide()
    }

    override fun showEmptyState() {
        summaryContainer.hide()
        emptyContainer.show()
    }

    override fun setOnShowAllVersionsClickListener(listener: (() -> Unit)?) {
        this.showAllVersionsClickListener = listener
    }

    override fun showReviewsProgress() {
        reviewsProgress.show()
        reviewsErrorContainer.hide()
    }

    override fun showReviewsError() {
        reviewsProgress.hide()
        reviewsErrorContainer.show()
    }

    override fun hideReviewsState() {
        reviewsProgress.hide()
        reviewsErrorContainer.hide()
    }

    override fun setOnRetryClickListener(listener: (() -> Unit)?) {
        this.retryClickListener = listener
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
        this.editionScopeClickListener = null
        this.buildScopeClickListener = null
        this.showAllVersionsClickListener = null
        this.retryClickListener = null
    }

}
