package com.tomclaw.appsend.screen.moderation.adapter.app

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.categories.CategoryItem
import kotlinx.parcelize.Parcelize

@Parcelize
class AppItem(
    override val id: Long,
    val appId: String,
    val icon: String?,
    val title: String,
    val version: String,
    val size: String,
    val rating: Float,
    val downloads: Int,
    val category: CategoryItem?,
    val openSource: Boolean,
    var hasMore: Boolean = false,
    var hasError: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable
