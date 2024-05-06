package com.tomclaw.appsend.screen.ratings

import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.screen.ratings.adapter.rating.RatingItem

interface RatingConverter {

    fun convert(entity: RatingEntity): RatingItem

}

class RatingConverterImpl : RatingConverter {

    override fun convert(entity: RatingEntity): RatingItem {
        return RatingItem(
            id = entity.rateId.toLong(),
            rateId = entity.rateId,
            score = entity.score,
            text = entity.text,
            time = entity.time,
            userId = entity.userId,
            userIcon = entity.userIcon,
        )
    }

}
