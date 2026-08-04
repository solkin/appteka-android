package com.tomclaw.appsend.screen.details.adapter.content_flags

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

/**
 * What this app was found to contain. Carried as codes rather than
 * resolved text so the item stays a plain data carrier — the view owns
 * the wording, the same way tags do.
 */
@Parcelize
data class ContentFlagsItem(
    override val id: Long,
    val codes: List<String>,
) : Item, Parcelable
