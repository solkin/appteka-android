package com.tomclaw.appsend.screen.users

import com.tomclaw.appsend.screen.users.adapter.UserItem
import com.tomclaw.appsend.screen.users.adapter.subscriber.SubscriberItem
import com.tomclaw.appsend.screen.users.api.PublisherEntity
import com.tomclaw.appsend.screen.users.api.SubscriberEntity
import com.tomclaw.appsend.screen.users.api.UserEntity
import java.util.concurrent.TimeUnit

interface UserConverter {

    fun convert(entity: UserEntity): UserItem

}

class UserConverterImpl : UserConverter {

    override fun convert(entity: UserEntity): UserItem {
        return when(entity) {
            is SubscriberEntity -> SubscriberItem(
                id = entity.rowId.toLong(),
                time = TimeUnit.SECONDS.toMillis(entity.time),
                user = entity.user,
            )
            is PublisherEntity -> SubscriberItem(
                id = entity.rowId.toLong(),
                time = TimeUnit.SECONDS.toMillis(entity.time),
                user = entity.user,
            )
            else -> throw IllegalArgumentException("Entity type is not supported: $entity")
        }
    }

}
