package com.tomclaw.appsend.screen.ratings

import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityPolicy
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.screen.ratings.adapter.rating.RatingItem
import com.tomclaw.appsend.user.api.UserBrief
import java.util.Locale

interface RatingConverter {

    fun convert(entity: RatingEntity, brief: UserBrief?): RatingItem

}

class RatingConverterImpl(
    private val locale: Locale
) : RatingConverter {

    override fun convert(entity: RatingEntity, brief: UserBrief?): RatingItem {
        // Server-resolved capability is the source of truth for the
        // delete affordance. Fall back to the legacy role+ownership
        // heuristic when it's missing so old servers keep working.
        val showRatingMenu = CapabilityPolicy.isAllowed(
            action = CapabilityAction.APP_RATING_DELETE,
            capabilities = entity.capabilities,
            allowOnUnknown = legacyCanDelete(brief, entity.userId),
        )
        return RatingItem(
            id = entity.rateId.toLong(),
            rateId = entity.rateId,
            score = entity.score,
            text = entity.text,
            time = entity.time * 1000,
            userId = entity.userId,
            userName = entity.userName.takeIf { !it.isNullOrBlank() }
                ?: entity.userIcon.label[locale.language]
                ?: entity.userIcon.label[DEFAULT_LOCALE].orEmpty(),
            userIcon = entity.userIcon,
            showRatingMenu = showRatingMenu,
        )
    }

    private fun legacyCanDelete(brief: UserBrief?, ratingUserId: Int): Boolean {
        val u = brief ?: return false
        return u.role >= LEGACY_ROLE_ADMIN || u.userId == ratingUserId
    }

}

// Legacy role threshold used only for backward-compat when the server
// has not yet started sending capabilities for this resource.
private const val LEGACY_ROLE_ADMIN = 200
