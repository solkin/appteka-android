package com.tomclaw.appsend.screen.details.adapter.scores

import com.tomclaw.appsend.core.RatingScope
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.util.adapter.ItemPresenter
import kotlin.math.max

class ScoresItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ScoresItemView, ScoresItem> {

    override fun bindView(view: ScoresItemView, item: ScoresItem, position: Int) {
        if (item.showScopeToggle) {
            when (item.selectedScope) {
                RatingScope.BUILD -> view.showScopeBuild()
                RatingScope.EDITION -> view.showScopeEdition()
            }
            view.setOnEditionScopeClickListener { listener.onReviewScopeSelected(RatingScope.EDITION) }
            view.setOnBuildScopeClickListener { listener.onReviewScopeSelected(RatingScope.BUILD) }
        } else {
            view.hideScopeToggle()
            view.setOnEditionScopeClickListener(null)
            view.setOnBuildScopeClickListener(null)
        }

        if (item.rateCount > 0) {
            view.showScoresSummary()
            view.setRating(item.rating)
            with(item.scores) {
                val maxValue = max(1, getMaxValue(five, four, three, two, one))
                view.setScores(
                    totalCount = item.rateCount,
                    five = 1 + 100 * five / maxValue,
                    four = 1 + 100 * four / maxValue,
                    three = 1 + 100 * three / maxValue,
                    two = 1 + 100 * two / maxValue,
                    one = 1 + 100 * one / maxValue
                )
            }
            view.setOnClickListener { listener.onScoresClick() }
        } else {
            view.showEmptyState()
            view.setOnClickListener(null)
        }
    }

    private fun getMaxValue(vararg values: Int): Int {
        var result = values[0]
        for (value in values) {
            result = max(result, value)
        }
        return result
    }

}
