package com.tomclaw.appsend.screen.reviews

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
            showRatingMenu = brief
                ?.let { it.role >= ROLE_ADMIN || it.userId == entity.rating.userId } ?: false,
        )
    }

}

private const val ROLE_ADMIN = 200
