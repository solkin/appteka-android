package com.tomclaw.appsend.screen.ratings

import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.screen.ratings.adapter.rating.RatingItem
import java.util.Locale

interface RatingConverter {

    fun convert(entity: RatingEntity): RatingItem

}

class RatingConverterImpl(
    private val locale: Locale
) : RatingConverter {

    override fun convert(entity: RatingEntity): RatingItem {
        return RatingItem(
            id = entity.rateId.toLong(),
            rateId = entity.rateId,
            score = entity.score,
            text = entity.text,
            time = entity.time,
            userId = entity.userId,
            userName = entity.userName.takeIf { !it.isNullOrBlank() }
                ?: entity.userIcon.label[locale.language]
                ?: entity.userIcon.label[DEFAULT_LOCALE].orEmpty(),
            userIcon = entity.userIcon,
        )
    }

}
