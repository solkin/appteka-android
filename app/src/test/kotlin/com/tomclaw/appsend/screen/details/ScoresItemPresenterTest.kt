package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.core.RatingScope
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItem
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItemPresenter
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItemView
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
                selectedScope = RatingScope.EDITION,
            ),
            position = 0,
        )

        assertFalse(view.scopeToggleVisible)
        assertNull(view.editionScopeClickListener)
        assertNull(view.buildScopeClickListener)
        assertTrue(view.summaryVisible)
        assertFalse(view.emptyVisible)
        assertEquals(3, view.totalCount)
        assertNotNull(view.clickListener)
    }

    @Test
    fun `build with zero reviews keeps toggle visible and shows empty state without dividing by zero`() {
        val view = FakeScoresView()
        val selectedScopes = mutableListOf<RatingScope>()
        val presenter = ScoresItemPresenter(recordingListener(selectedScopes))

        presenter.bindView(
            view = view,
            item = ScoresItem(
                id = 1,
                rateCount = 0,
                rating = 0f,
                scores = Scores(five = 0, four = 0, three = 0, two = 0, one = 0),
                showScopeToggle = true,
                selectedScope = RatingScope.BUILD,
            ),
            position = 0,
        )

        assertTrue(view.scopeToggleVisible)
        assertEquals(RatingScope.BUILD, view.selectedScope)
        assertFalse(view.summaryVisible)
        assertTrue(view.emptyVisible)
        assertNull(view.clickListener)

        view.editionScopeClickListener?.invoke()
        view.buildScopeClickListener?.invoke()
        assertEquals(listOf(RatingScope.EDITION, RatingScope.BUILD), selectedScopes)
    }

    @Test
    fun `rating scope parse falls back to edition for unknown or null values`() {
        assertEquals(RatingScope.BUILD, RatingScope.parse("BUILD"))
        assertEquals(RatingScope.EDITION, RatingScope.parse("EDITION"))
        assertEquals(RatingScope.EDITION, RatingScope.parse(null))
        assertEquals(RatingScope.EDITION, RatingScope.parse("unknown"))
    }

    private class FakeScoresView : ScoresItemView {
        var scopeToggleVisible = false
        var selectedScope: RatingScope? = null
        var summaryVisible = false
        var emptyVisible = false
        var totalCount: Int = -1
        var clickListener: (() -> Unit)? = null
        var editionScopeClickListener: (() -> Unit)? = null
        var buildScopeClickListener: (() -> Unit)? = null

        override fun showScopeEdition() {
            scopeToggleVisible = true
            selectedScope = RatingScope.EDITION
        }

        override fun showScopeBuild() {
            scopeToggleVisible = true
            selectedScope = RatingScope.BUILD
        }

        override fun hideScopeToggle() {
            scopeToggleVisible = false
        }

        override fun setOnEditionScopeClickListener(listener: (() -> Unit)?) {
            editionScopeClickListener = listener
        }

        override fun setOnBuildScopeClickListener(listener: (() -> Unit)?) {
            buildScopeClickListener = listener
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

    private fun noopListener(): ItemListener = recordingListener(mutableListOf())

    private fun recordingListener(selectedScopes: MutableList<RatingScope>): ItemListener =
        requireNotNull(
            ItemListener::class.java.cast(
                Proxy.newProxyInstance(
                    ItemListener::class.java.classLoader,
                    arrayOf(ItemListener::class.java),
                ) { _, method, args ->
                    if (method.name == "onReviewScopeSelected" && args != null) {
                        selectedScopes += args[0] as RatingScope
                    }
                    null
                }
            )
        )

}
