package com.tomclaw.appsend.screen.chat.adapter.loadmore

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoadMoreItem(
    override val id: Long,
    val msgId: Int,
) : Item, Parcelable

const val LOAD_MORE_ITEM_ID: Long = -1L
