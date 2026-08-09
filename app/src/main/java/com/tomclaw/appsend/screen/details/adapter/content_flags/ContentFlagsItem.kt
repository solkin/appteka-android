package com.tomclaw.appsend.screen.details.adapter.content_flags

import android.os.Parcelable
import com.tomclaw.appsend.core.content.ContentFlag
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

/**
 * What this app was found to contain, worded by the server. The app has
 * no vocabulary to resolve codes against, so the wording travels with
 * them all the way to the chip.
 */
@Parcelize
data class ContentFlagsItem(
    override val id: Long,
    val flags: List<ContentFlag>,
) : Item, Parcelable
