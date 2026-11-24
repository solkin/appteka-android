package com.tomclaw.appsend.screen.details.adapter.scores

/**
 * State of the review list that follows the scores block. Only the build
 * scope loads its reviews separately, so edition scope is always [READY].
 */
enum class ReviewsLoadState {
    READY,
    LOADING,
    ERROR,
}
