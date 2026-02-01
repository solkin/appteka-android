package com.tomclaw.appsend.screen.details.adapter.scores

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import kotlin.math.max

class ScoresItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<ScoresItemView, ScoresItem> {

    override fun bindView(view: ScoresItemView, item: ScoresItem, position: Int) {
        view.setRating(item.rating)
        with(item.scores) {
            val maxValue = getMaxValue(five, four, three, two, one)
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
    }

    private fun getMaxValue(vararg values: Int): Int {
        var result = values[0]
        for (value in values) {
            result = max(result, value)
        }
        return result
    }

}
