package com.tomclaw.appsend.screen.reviews

import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityPolicy
import com.tomclaw.appsend.screen.reviews.adapter.review.ReviewItem
import com.tomclaw.appsend.screen.reviews.api.ReviewEntity
import com.tomclaw.appsend.user.api.UserBrief
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

interface ReviewConverter {

    fun convert(entity: ReviewEntity, brief: UserBrief?): ReviewItem

}

class ReviewConverterImpl() : ReviewConverter {

    private var id = AtomicLong(1)

    override fun convert(entity: ReviewEntity, brief: UserBrief?): ReviewItem {
        val showRatingMenu = CapabilityPolicy.isAllowed(
            action = CapabilityAction.APP_RATING_DELETE,
            capabilities = entity.rating.capabilities,
            allowOnUnknown = legacyCanDelete(brief, entity.rating.user.id),
        )
        return ReviewItem(
            id = id.incrementAndGet(),
            appId = entity.file.appId,
            rateId = entity.rating.rateId,
            icon = entity.file.icon,
            title = entity.file.title,
            version = entity.file.verName,
            rating = entity.rating.score.toFloat(),
            text = entity.rating.text,
            time = TimeUnit.SECONDS.toMillis(entity.rating.time),
            showRatingMenu = showRatingMenu,
        )
    }

    private fun legacyCanDelete(brief: UserBrief?, ratingUserId: Int): Boolean {
        val u = brief ?: return false
        return u.role >= LEGACY_ROLE_ADMIN || u.id == ratingUserId
    }

}

private const val LEGACY_ROLE_ADMIN = 200
