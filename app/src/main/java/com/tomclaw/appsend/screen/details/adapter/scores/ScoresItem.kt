package com.tomclaw.appsend.screen.details.adapter.scores

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.details.api.Scores
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScoresItem(
    override val id: Long,
    val rateCount: Int,
    val rating: Float,
    val scores: Scores,
) : Item, Parcelable
