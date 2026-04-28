package com.tomclaw.appsend.screen.ratings

import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityPolicy
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.screen.ratings.adapter.rating.RatingItem
import com.tomclaw.appsend.user.api.UserBrief

interface RatingConverter {

    fun convert(entity: RatingEntity, brief: UserBrief?): RatingItem

}

class RatingConverterImpl : RatingConverter {

    override fun convert(entity: RatingEntity, brief: UserBrief?): RatingItem {
        // Server-resolved capability is the source of truth for the
        // delete affordance. Fall back to the legacy role+ownership
        // heuristic when it's missing so old servers keep working.
        val showRatingMenu = CapabilityPolicy.isAllowed(
            action = CapabilityAction.APP_RATING_DELETE,
            capabilities = entity.capabilities,
            allowOnUnknown = legacyCanDelete(brief, entity.user.id),
        )
        return RatingItem(
            id = entity.rateId.toLong(),
            rateId = entity.rateId,
            score = entity.score,
            text = entity.text,
            time = entity.time * 1000,
            user = entity.user,
            showRatingMenu = showRatingMenu,
        )
    }

    private fun legacyCanDelete(brief: UserBrief?, ratingUserId: Int): Boolean {
        val u = brief ?: return false
        return u.role >= LEGACY_ROLE_ADMIN || u.id == ratingUserId
    }

}

// Legacy role threshold used only for backward-compat when the server
// has not yet started sending capabilities for this resource.
private const val LEGACY_ROLE_ADMIN = 200
