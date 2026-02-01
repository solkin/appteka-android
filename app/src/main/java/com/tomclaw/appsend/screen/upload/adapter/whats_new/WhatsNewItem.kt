package com.tomclaw.appsend.screen.upload.adapter.whats_new

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class WhatsNewItem(
    override val id: Long,
    val text: String,
) : Item, Parcelable
