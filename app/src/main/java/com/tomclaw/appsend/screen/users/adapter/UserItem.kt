package com.tomclaw.appsend.screen.users.adapter

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.user.api.UserBrief

interface UserItem : Item, Parcelable {
    val user: UserBrief
    var hasMore: Boolean
    var hasProgress: Boolean
}