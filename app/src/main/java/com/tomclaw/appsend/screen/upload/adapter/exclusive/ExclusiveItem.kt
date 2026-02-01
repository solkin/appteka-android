package com.tomclaw.appsend.screen.upload.adapter.exclusive

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ExclusiveItem(
    override val id: Long,
    val value: Boolean,
) : Item, Parcelable
