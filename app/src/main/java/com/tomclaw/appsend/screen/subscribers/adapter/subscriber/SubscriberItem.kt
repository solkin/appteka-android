package com.tomclaw.appsend.screen.subscribers.adapter.subscriber

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
class SubscriberItem(
    override val id: Long,
    val time: Long,
    val user: UserBrief,
    var hasMore: Boolean = false,
    var hasError: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable
