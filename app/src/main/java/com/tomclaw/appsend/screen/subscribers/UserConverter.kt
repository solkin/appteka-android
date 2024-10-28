package com.tomclaw.appsend.screen.subscribers

import com.tomclaw.appsend.screen.subscribers.adapter.subscriber.SubscriberItem
import com.tomclaw.appsend.screen.subscribers.api.SubscriberEntity

interface UserConverter {

    fun convert(entity: SubscriberEntity): SubscriberItem

}

class UserConverterImpl : UserConverter {

    override fun convert(entity: SubscriberEntity): SubscriberItem {
        return SubscriberItem(
            id = entity.rowId.toLong(),
            time = entity.time,
            user = entity.user,
        )
    }

}
