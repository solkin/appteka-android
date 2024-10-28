package com.tomclaw.appsend.screen.subscribers

import com.tomclaw.appsend.screen.subscribers.adapter.user.UserItem
import com.tomclaw.appsend.screen.subscribers.api.SubscriberEntity

interface UserConverter {

    fun convert(entity: SubscriberEntity): UserItem

}

class UserConverterImpl : UserConverter {

    override fun convert(entity: SubscriberEntity): UserItem {
        return UserItem(
            id = entity.rowId.toLong(),
            time = entity.time,
            user = entity.user,
        )
    }

}
