package com.tomclaw.appsend.screen.ratings

import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.screen.ratings.adapter.rating.RatingItem
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.RoleHelper
import java.util.Locale

interface RatingConverter {

    fun convert(entity: RatingEntity, brief: UserBrief?): RatingItem

}

class RatingConverterImpl(
    private val locale: Locale
) : RatingConverter {

    override fun convert(entity: RatingEntity, brief: UserBrief?): RatingItem {
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
            showRatingMenu = brief?.let { it.role >= RoleHelper.ROLE_ADMIN } ?: false,
        )
    }

}
