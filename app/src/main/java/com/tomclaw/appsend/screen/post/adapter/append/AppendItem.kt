package com.tomclaw.appsend.screen.post.adapter.append

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppendItem(
    override val id: Long,
) : Item, Parcelable
