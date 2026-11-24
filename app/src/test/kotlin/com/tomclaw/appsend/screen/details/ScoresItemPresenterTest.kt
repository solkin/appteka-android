package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItem
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItemPresenter
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItemView
import com.tomclaw.appsend.screen.details.api.RATING_SCOPE_BUILD
import com.tomclaw.appsend.screen.details.api.RATING_SCOPE_EDITION
import com.tomclaw.appsend.screen.details.api.Scores
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class ScoresItemPresenterTest {

    @Test
    fun `single-build edition hides scope toggle and shows summary`() {
        val view = FakeScoresView()
        val presenter = ScoresItemPresenter(noopListener())

        presenter.bindView(
            view = view,
            item = ScoresItem(
                id = 1,
                rateCount = 3,
                rating = 4.0f,
                scores = Scores(five = 1, four = 1, three = 1, two = 0, one = 0),
                showScopeToggle = false,
                selectedScope = RATING_SCOPE_EDITION,
            ),
            position = 0,
        )

        assertFalse(view.scopeToggleVisible)
        assertTrue(view.summaryVisible)
        assertFalse(view.emptyVisible)
        assertEquals(3, view.totalCount)
        assertNotNull(view.clickListener)
    }

    @Test
    fun `build with zero reviews keeps toggle visible and shows empty state without dividing by zero`() {
        val view = FakeScoresView()
        val presenter = ScoresItemPresenter(noopListener())

        presenter.bindView(
            view = view,
            item = ScoresItem(
                id = 1,
                rateCount = 0,
                rating = 0f,
                scores = Scores(five = 0, four = 0, three = 0, two = 0, one = 0),
                showScopeToggle = true,
                selectedScope = RATING_SCOPE_BUILD,
            ),
            position = 0,
        )

        assertTrue(view.scopeToggleVisible)
        assertEquals(RATING_SCOPE_BUILD, view.selectedScope)
        assertFalse(view.summaryVisible)
        assertTrue(view.emptyVisible)
        assertNull(view.clickListener)
    }

    private class FakeScoresView : ScoresItemView {
        var scopeToggleVisible = false
        var selectedScope: String? = null
        var summaryVisible = false
        var emptyVisible = false
        var totalCount: Int = -1
        var clickListener: (() -> Unit)? = null
        var scopeListener: ((String) -> Unit)? = null

        override fun showScopeToggle(selectedScope: String) {
            scopeToggleVisible = true
            this.selectedScope = selectedScope
        }

        override fun hideScopeToggle() {
            scopeToggleVisible = false
        }

        override fun setOnScopeSelectedListener(listener: ((String) -> Unit)?) {
            scopeListener = listener
        }

        override fun showScoresSummary() {
            summaryVisible = true
            emptyVisible = false
        }

        override fun showEmptyState() {
            summaryVisible = false
            emptyVisible = true
        }

        override fun setRating(rating: Float) = Unit

        override fun setScores(totalCount: Int, five: Int, four: Int, three: Int, two: Int, one: Int) {
            this.totalCount = totalCount
        }

        override fun setOnClickListener(listener: (() -> Unit)?) {
            clickListener = listener
        }
    }

    private fun noopListener(): ItemListener = requireNotNull(
        ItemListener::class.java.cast(
            Proxy.newProxyInstance(
                ItemListener::class.java.classLoader,
                arrayOf(ItemListener::class.java),
            ) { _, _, _ -> null }
        )
    )

}
