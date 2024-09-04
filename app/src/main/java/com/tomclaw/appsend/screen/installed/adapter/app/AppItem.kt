package com.tomclaw.appsend.screen.installed.adapter.app

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
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
    var updateAppId: String?,
    val isUserApp: Boolean,
    var isNew: Boolean = false,
) : Item, Parcelable
