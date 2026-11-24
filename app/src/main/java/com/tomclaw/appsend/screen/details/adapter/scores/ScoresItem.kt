package com.tomclaw.appsend.screen.details.adapter.scores

import android.os.Parcelable
import com.tomclaw.appsend.core.RatingScope
import com.tomclaw.appsend.screen.details.api.Scores
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScoresItem(
    override val id: Long,
    val rateCount: Int,
    val rating: Float,
    val scores: Scores,
    val showScopeToggle: Boolean = false,
    val selectedScope: RatingScope = RatingScope.EDITION,
) : Item, Parcelable
