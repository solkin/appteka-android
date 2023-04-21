package com.tomclaw.appsend.upload

import android.os.Parcelable
import com.tomclaw.appsend.categories.CategoryItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetaInfo(
    val category: CategoryItem,
    val description: String,
    val whatsNew: String,
    val exclusive: Boolean,
    val openSource: Boolean,
    val sourceUrl: String?
) : Parcelable
