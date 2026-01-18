package com.tomclaw.appsend.screen.favorite.adapter.app

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
    val status: Int,
    val category: CategoryItem?,
    val exclusive: Boolean,
    val openSource: Boolean,
    val isAbiCompatible: Boolean,
    var isInstalled: Boolean = false,
    var isUpdatable: Boolean = false,
    var isNew: Boolean = false,
    var hasMore: Boolean = false,
    var hasError: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable
