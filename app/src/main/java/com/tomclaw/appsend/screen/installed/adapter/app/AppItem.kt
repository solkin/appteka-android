package com.tomclaw.appsend.screen.installed.adapter.app

import android.os.Parcelable
import com.tomclaw.appsend.delivery.DeliveryState
import com.tomclaw.appsend.screen.installed.api.UpdateEntity
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
class AppItem(
    override val id: Long,
    val icon: String?,
    val title: String,
    val version: String,
    val size: Long,
    val installTime: Long,
    val updateTime: Long,
    val packageName: String,
    val path: String?,
    val update: UpdateEntity?,
    val otherEdition: UpdateEntity?,
    val isUserApp: Boolean,
    // Signing key of the installed app, null if unreadable.
    val editionKey: String?,
    var isNew: Boolean = false,
    /** How far [update] has got on its way to the device. */
    var updateState: DeliveryState = DeliveryState.Idle,
) : Item, Parcelable
