package com.tomclaw.appsend.screen.subscribers

import com.tomclaw.appsend.screen.subscribers.adapter.subscriber.SubscriberItem
import com.tomclaw.appsend.screen.subscribers.api.SubscriberEntity
import java.util.concurrent.TimeUnit

interface UserConverter {

    fun convert(entity: SubscriberEntity): SubscriberItem

}

class UserConverterImpl : UserConverter {

    override fun convert(entity: SubscriberEntity): SubscriberItem {
        return SubscriberItem(
            id = entity.rowId.toLong(),
            time = TimeUnit.SECONDS.toMillis(entity.time),
            user = entity.user,
        )
    }

}
