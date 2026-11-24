package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.core.RatingScope
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.scores.ReviewsLoadState
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
    fun `build reviews loading and error states are shown below the summary`() {
        val view = FakeScoresView()
        val retries = mutableListOf<Unit>()
        val presenter = ScoresItemPresenter(recordingListener(mutableListOf(), retries))
        val item = ScoresItem(
            id = 1,
            rateCount = 2,
            rating = 4.5f,
            scores = Scores(five = 1, four = 1, three = 0, two = 0, one = 0),
            showScopeToggle = true,
            selectedScope = RatingScope.BUILD,
            buildVersionName = "2.0",
            reviewsState = ReviewsLoadState.LOADING,
        )

        presenter.bindView(view, item, position = 0)
        assertEquals("2.0", view.buildVersionName)
        assertTrue(view.summaryVisible)
        assertEquals(ReviewsLoadState.LOADING, view.reviewsState)

        presenter.bindView(view, item.copy(reviewsState = ReviewsLoadState.ERROR), position = 0)
        assertEquals(ReviewsLoadState.ERROR, view.reviewsState)
        view.retryClickListener?.invoke()
        assertEquals(1, retries.size)

        presenter.bindView(view, item.copy(reviewsState = ReviewsLoadState.READY), position = 0)
        assertEquals(ReviewsLoadState.READY, view.reviewsState)
    }

    @Test
    fun `empty build state offers to switch back to all versions`() {
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

        assertTrue(view.emptyVisible)
        view.showAllVersionsClickListener?.invoke()
        assertEquals(listOf(RatingScope.EDITION), selectedScopes)
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
        var showAllVersionsClickListener: (() -> Unit)? = null
        var retryClickListener: (() -> Unit)? = null
        var buildVersionName: String? = null
        var reviewsState: ReviewsLoadState? = null

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

        override fun setBuildScopeVersion(versionName: String) {
            buildVersionName = versionName
        }

        override fun setOnShowAllVersionsClickListener(listener: (() -> Unit)?) {
            showAllVersionsClickListener = listener
        }

        override fun showReviewsProgress() {
            reviewsState = ReviewsLoadState.LOADING
        }

        override fun showReviewsError() {
            reviewsState = ReviewsLoadState.ERROR
        }

        override fun hideReviewsState() {
            reviewsState = ReviewsLoadState.READY
        }

        override fun setOnRetryClickListener(listener: (() -> Unit)?) {
            retryClickListener = listener
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

    private fun recordingListener(
        selectedScopes: MutableList<RatingScope>,
        retries: MutableList<Unit> = mutableListOf(),
    ): ItemListener =
        requireNotNull(
            ItemListener::class.java.cast(
                Proxy.newProxyInstance(
                    ItemListener::class.java.classLoader,
                    arrayOf(ItemListener::class.java),
                ) { _, method, args ->
                    if (method.name == "onReviewScopeSelected" && args != null) {
                        selectedScopes += args[0] as RatingScope
                    }
                    if (method.name == "onBuildReviewsRetry") {
                        retries += Unit
                    }
                    null
                }
            )
        )

}
